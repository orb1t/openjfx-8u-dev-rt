/*
 *  Copyright (C) 2003, 2004, 2005, 2006, 2007, 2008, 2009 Apple Inc. All rights reserved.
 *  Copyright (C) 2007 Eric Seidel <eric@webkit.org>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

#include "config.h"
#include "StackBounds.h"

#if OS(DARWIN)

#include <mach/task.h>
#include <mach/thread_act.h>
#include <pthread.h>

#elif OS(WINDOWS)

#include <windows.h>

#elif OS(SOLARIS)

#include <thread.h>

#elif OS(UNIX)

#include <pthread.h>
#if HAVE(PTHREAD_NP_H)
#include <pthread_np.h>
#endif

#endif

namespace WTF {

#if PLATFORM(JAVA)
// 16K is a safe value to guard java stack red zone
#define JAVA_RED_ZONE 0x4000
#if OS(WINDOWS)
// This is safe for the default stack sizes in all supported Windows
// configurations, but is not safe for stack sizes lower than the default.
#if CPU(X86_64)
static const ptrdiff_t estimatedStackSize = 1024 * 1024;
#else
// estimatedStackSize needs to be a little greater than 256KB to keep
// Interpreter::StackPolicy::StackPolicy happy and less than 320KB
// to play safe against the default stack size on 32 bit Windows.
static const ptrdiff_t estimatedStackSize = 272 * 1024;
#endif
#endif
#endif

#if OS(DARWIN)

StackBounds StackBounds::newThreadStackBounds(PlatformThreadHandle thread)
{
    void* origin = pthread_get_stackaddr_np(thread);
    rlim_t size = pthread_get_stacksize_np(thread);
    void* bound = static_cast<char*>(origin) - size;
    return StackBounds { origin, bound };
}

StackBounds StackBounds::currentThreadStackBoundsInternal()
{
    if (pthread_main_np()) {
        // FIXME: <rdar://problem/13741204>
        // pthread_get_size lies to us when we're the main thread, use get_rlimit instead
        void* origin = pthread_get_stackaddr_np(pthread_self());
        rlimit limit;
        getrlimit(RLIMIT_STACK, &limit);
        rlim_t size = limit.rlim_cur;
        void* bound = static_cast<char*>(origin) - size;
#if PLATFORM(JAVA)
        bound = static_cast<char*>(bound) + JAVA_RED_ZONE;
#endif
        return StackBounds { origin, bound };
    }
    return newThreadStackBounds(pthread_self());
}

#elif OS(SOLARIS)

StackBounds StackBounds::currentThreadStackBoundsInternal()
{
    stack_t s;
    thr_stksegment(&s);
    void* origin = s.ss_sp;
    void* bound = static_cast<char*>(origin) - s.ss_size;
    return StackBounds { origin, bound };
}

#elif OS(UNIX)

#if OS(OPENBSD)

StackBounds StackBounds::newThreadStackBounds(PlatformThreadHandle thread)
{
    stack_t stack;
    pthread_stackseg_np(thread, &stack);
    void* origin = stack.ss_sp;
#if CPU(HPPA)
    void* bound = static_cast<char*>(origin) + stack.ss_size;
#else
    void* bound = static_cast<char*>(origin) - stack.ss_size;
#endif
    return StackBounds { origin, bound };
}

#else // !OS(OPENBSD)

StackBounds StackBounds::newThreadStackBounds(PlatformThreadHandle thread)
{
    void* bound = nullptr;
    size_t stackSize = 0;

    pthread_attr_t sattr;
    pthread_attr_init(&sattr);
#if HAVE(PTHREAD_NP_H) || OS(NETBSD)
    // e.g. on FreeBSD 5.4, neundorf@kde.org
    pthread_attr_get_np(thread, &sattr);
#else
    // FIXME: this function is non-portable; other POSIX systems may have different np alternatives
    pthread_getattr_np(thread, &sattr);
#endif
    int rc = pthread_attr_getstack(&sattr, &bound, &stackSize);
    UNUSED_PARAM(rc);
    ASSERT(bound);
    pthread_attr_destroy(&sattr);
    void* origin = static_cast<char*>(bound) + stackSize;
#if PLATFORM(JAVA)
    bound = static_cast<char*>(bound) + JAVA_RED_ZONE;
#endif
    return StackBounds { origin, bound };
}

#endif // OS(OPENBSD)

StackBounds StackBounds::currentThreadStackBoundsInternal()
{
    return newThreadStackBounds(pthread_self());
}

#elif OS(WINDOWS)

StackBounds StackBounds::currentThreadStackBoundsInternal()
{
    MEMORY_BASIC_INFORMATION stackOrigin = { 0 };
    VirtualQuery(&stackOrigin, &stackOrigin, sizeof(stackOrigin));
    // stackOrigin.AllocationBase points to the reserved stack memory base address.

    const LPVOID theAllocBase = stackOrigin.AllocationBase;
    void* origin = static_cast<char*>(stackOrigin.BaseAddress) + stackOrigin.RegionSize;

    // The stack on Windows consists out of three parts (uncommitted memory, a guard page and present
    // committed memory). The 3 regions have different BaseAddresses but all have the same AllocationBase
    // since they are all from the same VirtualAlloc. The 3 regions are laid out in memory (from high to
    // low) as follows:
    //
    //    High |-------------------|  -----
    //         | committedMemory   |    ^
    //         |-------------------|    |
    //         | guardPage         | reserved memory for the stack
    //         |-------------------|    |
    //         | uncommittedMemory |    v
    //    Low  |-------------------|  ----- <--- stackOrigin.AllocationBase
    //
    // See http://msdn.microsoft.com/en-us/library/ms686774%28VS.85%29.aspx for more information.

    // look for uncommited memory block.
    MEMORY_BASIC_INFORMATION uncommittedMemory = { 0 };
    LPVOID a = stackOrigin.AllocationBase;

    do {
        size_t ret = VirtualQuery(a, &uncommittedMemory, sizeof(uncommittedMemory));
        ASSERT(ret != 0);
        a = (LPVOID)((static_cast<char*>(a)) + uncommittedMemory.RegionSize);
    } while (theAllocBase == uncommittedMemory.AllocationBase &&
        uncommittedMemory.State != MEM_RESERVE);

    MEMORY_BASIC_INFORMATION guardPage{ 0 };
    VirtualQuery(static_cast<char*>(uncommittedMemory.BaseAddress) + uncommittedMemory.RegionSize, &guardPage, sizeof(guardPage));
    ASSERT(guardPage.Protect & PAGE_GUARD);

    void* endOfStack = stackOrigin.AllocationBase;

#ifndef NDEBUG
    MEMORY_BASIC_INFORMATION committedMemory;
    VirtualQuery(static_cast<char*>(guardPage.BaseAddress) + guardPage.RegionSize, &committedMemory, sizeof(committedMemory));
    ASSERT(committedMemory.State == MEM_COMMIT);

    void* computedEnd = static_cast<char*>(origin) - (uncommittedMemory.RegionSize + guardPage.RegionSize + committedMemory.RegionSize);

    ASSERT(stackOrigin.AllocationBase == uncommittedMemory.AllocationBase);
    ASSERT(stackOrigin.AllocationBase == guardPage.AllocationBase);
    ASSERT(stackOrigin.AllocationBase == committedMemory.AllocationBase);
    // TODO: refine the sanity checks below.
    //ASSERT(stackOrigin.AllocationBase == uncommittedMemory.BaseAddress);
    //ASSERT(endOfStack == computedEnd);
#endif // NDEBUG
    void* bound = static_cast<char*>(endOfStack) + guardPage.RegionSize;
    return StackBounds { origin, bound };
}

#else
#error Need a way to get the stack bounds on this platform
#endif

} // namespace WTF

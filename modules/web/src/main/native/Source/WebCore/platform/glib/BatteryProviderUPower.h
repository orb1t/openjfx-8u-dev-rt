/*
 * Copyright (C) 2013 Igalia S.L.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301, USA.
 */

#ifndef BatteryProviderUPower_h
#define BatteryProviderUPower_h

#if ENABLE(BATTERY_STATUS)

#include <libupower-glib/upower.h>
#include <wtf/glib/GRefPtr.h>

namespace WebCore {

class BatteryProviderUPowerClient;

class BatteryProviderUPower {
public:
    BatteryProviderUPower(BatteryProviderUPowerClient*);

    void startUpdating();
    void stopUpdating();

    void updateBatteryStatus();

private:
    BatteryProviderUPowerClient* m_client;

    GRefPtr<UpClient> m_upowerClient;

    bool m_isUpdating;
};

}

#endif // ENABLE(BATTERY_STATUS)

#endif // BatteryProviderUPower_h

set(IMAGE_DIFF_SOURCES
    ${IMAGE_DIFF_DIR}/efl/ImageDiff.cpp
)

list(APPEND IMAGE_DIFF_SYSTEM_INCLUDE_DIRECTORIES
    ${ECORE_EVAS_INCLUDE_DIRS}
    ${ECORE_IMF_INCLUDE_DIRS}
)

list(APPEND IMAGE_DIFF_LIBRARIES
    ${ECORE_EVAS_LIBRARIES}
)

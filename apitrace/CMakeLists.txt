project ( ApitraceAutoBuild NONE )
cmake_minimum_required( VERSION 2.8.11 )

include ( ExternalProject )

set ( ApitraceBinDir "${CMAKE_BINARY_DIR}/apitrace_bin" )

# All about Apitrace
set ( APITRACE_APPIMAGE "${CMAKE_BINARY_DIR}/bin/Apitrace.AppImage" )
set ( APITRACE_APPDIR "${CMAKE_BINARY_DIR}/apitrace_bin" )

# Visual things
set ( FREEDESKTOP_APPNAME "QApitrace" )
set ( FREEDESKTOP_APPICON "apitrace" )
set ( FREEDESKTOP_APPFILE "qapitrace" )
set ( APPIMAGE_EXEC "bin/qapitrace" )

# AppImage stuff
set ( APPIMG_DIR "${CMAKE_BINARY_DIR}/appimg_src" )
set ( APPIMAGEAST_EXEC "${APPIMG_DIR}/AppImageAssistant" )

ExternalProject_Add(
    Apitrace

    GIT_REPOSITORY "https://github.com/apitrace/apitrace.git"
    GIT_TAG "master"

    SOURCE_DIR "${CMAKE_BINARY_DIR}/src"

    CMAKE_GENERATOR "Ninja"
    CMAKE_ARGS
        -DCMAKE_INSTALL_PREFIX=${ApitraceBinDir}
    )

# Remove some files that stop everything in AppImage
add_custom_target ( AppImageRemove
    DEPENDS Apitrace )
add_custom_command ( TARGET AppImageRemove
    COMMAND ${CMAKE_COMMAND} -E remove "${APITRACE_APPIMAGE}"
    )

# Make directories, because AppImage does not do that itself
add_custom_target ( AppImageMkdir
    DEPENDS AppImageRemove )

add_custom_command ( TARGET AppImageMkdir
    COMMAND ${CMAKE_COMMAND} -E make_directory "${CMAKE_BINARY_DIR}/bin"
    )

# Clone and compile AppImageKit
ExternalProject_Add(
    AppImageBuild

    DEPENDS AppImageMkdir

    GIT_REPOSITORY "https://github.com/probonopd/AppImageKit.git"
    GIT_TAG "master"

    SOURCE_DIR "${APPIMG_DIR}"
    BUILD_IN_SOURCE 1

    CMAKE_GENERATOR "Ninja"

    INSTALL_COMMAND true # Because the configuration is not good...
    UPDATE_COMMAND true
    )

# Configure .desktop and icon files, really mostly copying
configure_file (
    ${CMAKE_SOURCE_DIR}/apitrace.desktop
    ${APITRACE_APPDIR}/apitrace.desktop
    @ONLY )
configure_file (
    ${CMAKE_SOURCE_DIR}/apitrace.png
    ${APITRACE_APPDIR}/apitrace.png
    @ONLY )
configure_file (
    ${CMAKE_SOURCE_DIR}/startup.sh.in
    ${APITRACE_APPDIR}/AppRun
    @ONLY )

# Finally build the AppImage
add_custom_target ( ApitraceAppImageBuild
    ALL
    DEPENDS AppImageBuild
    SOURCES
        ${CMAKE_SOURCE_DIR}/apitrace.desktop
        ${CMAKE_SOURCE_DIR}/apitrace.png
    )

add_custom_command ( TARGET ApitraceAppImageBuild
    COMMAND ${APPIMAGEAST_EXEC} "${APITRACE_APPDIR}" "${APITRACE_APPIMAGE}"
    )

install (
    FILES "${APITRACE_APPIMAGE}"
    DESTINATION .
    )

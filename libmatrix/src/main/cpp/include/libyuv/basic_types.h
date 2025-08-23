

#ifndef INCLUDE_LIBYUV_BASIC_TYPES_H_
#define INCLUDE_LIBYUV_BASIC_TYPES_H_

#include <stddef.h>

#if !defined(INT_TYPES_DEFINED) && !defined(GG_LONGLONG)
#define INT_TYPES_DEFINED

#if defined(_MSC_VER) && (_MSC_VER < 1600)
#include <sys/types.h>
typedef unsigned __int64 uint64_t;
typedef __int64 int64_t;
typedef unsigned int uint32_t;
typedef int int32_t;
typedef unsigned short uint16_t;
typedef short int16_t;
typedef unsigned char uint8_t;
typedef signed char int8_t;
#else
#include <stdint.h>
#endif
#ifdef LIBYUV_LEGACY_TYPES
typedef uint64_t uint64;
typedef int64_t int64;
typedef uint32_t uint32;
typedef int32_t int32;
typedef uint16_t uint16;
typedef int16_t int16;
typedef uint8_t uint8;
typedef int8_t int8;
#endif
#endif

#if !defined(LIBYUV_API)
#if defined(_WIN32) || defined(__CYGWIN__)
#if defined(LIBYUV_BUILDING_SHARED_LIBRARY)
#define LIBYUV_API __declspec(dllexport)
#elif defined(LIBYUV_USING_SHARED_LIBRARY)
#define LIBYUV_API __declspec(dllimport)
#else
#define LIBYUV_API
#endif
#elif defined(__GNUC__) && (__GNUC__ >= 4) && !defined(__APPLE__) && \
    (defined(LIBYUV_BUILDING_SHARED_LIBRARY) ||                      \
     defined(LIBYUV_USING_SHARED_LIBRARY))
#define LIBYUV_API __attribute__((visibility("default")))
#else
#define LIBYUV_API
#endif
#endif

#define LIBYUV_BOOL int
#define LIBYUV_FALSE 0
#define LIBYUV_TRUE 1

#endif

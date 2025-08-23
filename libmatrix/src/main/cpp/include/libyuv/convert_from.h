

#ifndef INCLUDE_LIBYUV_CONVERT_FROM_H_
#define INCLUDE_LIBYUV_CONVERT_FROM_H_

#include "libyuv/basic_types.h"
#include "libyuv/rotate.h"

#ifdef __cplusplus
namespace libyuv {
extern "C" {
#endif

#define H420ToH010 I420ToI010
int I420ToI010(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint16_t* dst_y,
               int dst_stride_y,
               uint16_t* dst_u,
               int dst_stride_u,
               uint16_t* dst_v,
               int dst_stride_v,
               int width,
               int height);

LIBYUV_API
int I420ToI422(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_u,
               int dst_stride_u,
               uint8_t* dst_v,
               int dst_stride_v,
               int width,
               int height);

LIBYUV_API
int I420ToI444(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_u,
               int dst_stride_u,
               uint8_t* dst_v,
               int dst_stride_v,
               int width,
               int height);

LIBYUV_API
int I400Copy(const uint8_t* src_y,
             int src_stride_y,
             uint8_t* dst_y,
             int dst_stride_y,
             int width,
             int height);

LIBYUV_API
int I420ToNV12(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_uv,
               int dst_stride_uv,
               int width,
               int height);

LIBYUV_API
int I420ToNV21(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_vu,
               int dst_stride_vu,
               int width,
               int height);

LIBYUV_API
int I420ToYUY2(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_yuy2,
               int dst_stride_yuy2,
               int width,
               int height);

LIBYUV_API
int I420ToUYVY(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_uyvy,
               int dst_stride_uyvy,
               int width,
               int height);

LIBYUV_API
int I420ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

LIBYUV_API
int I420ToBGRA(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_bgra,
               int dst_stride_bgra,
               int width,
               int height);

LIBYUV_API
int I420ToABGR(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

LIBYUV_API
int I420ToRGBA(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_rgba,
               int dst_stride_rgba,
               int width,
               int height);

LIBYUV_API
int I420ToRGB24(const uint8_t* src_y,
                int src_stride_y,
                const uint8_t* src_u,
                int src_stride_u,
                const uint8_t* src_v,
                int src_stride_v,
                uint8_t* dst_rgb24,
                int dst_stride_rgb24,
                int width,
                int height);

LIBYUV_API
int I420ToRAW(const uint8_t* src_y,
              int src_stride_y,
              const uint8_t* src_u,
              int src_stride_u,
              const uint8_t* src_v,
              int src_stride_v,
              uint8_t* dst_raw,
              int dst_stride_raw,
              int width,
              int height);

LIBYUV_API
int H420ToRGB24(const uint8_t* src_y,
                int src_stride_y,
                const uint8_t* src_u,
                int src_stride_u,
                const uint8_t* src_v,
                int src_stride_v,
                uint8_t* dst_rgb24,
                int dst_stride_rgb24,
                int width,
                int height);

LIBYUV_API
int H420ToRAW(const uint8_t* src_y,
              int src_stride_y,
              const uint8_t* src_u,
              int src_stride_u,
              const uint8_t* src_v,
              int src_stride_v,
              uint8_t* dst_raw,
              int dst_stride_raw,
              int width,
              int height);

LIBYUV_API
int I420ToRGB565(const uint8_t* src_y,
                 int src_stride_y,
                 const uint8_t* src_u,
                 int src_stride_u,
                 const uint8_t* src_v,
                 int src_stride_v,
                 uint8_t* dst_rgb565,
                 int dst_stride_rgb565,
                 int width,
                 int height);

LIBYUV_API
int J420ToRGB565(const uint8_t* src_y,
                 int src_stride_y,
                 const uint8_t* src_u,
                 int src_stride_u,
                 const uint8_t* src_v,
                 int src_stride_v,
                 uint8_t* dst_rgb565,
                 int dst_stride_rgb565,
                 int width,
                 int height);

LIBYUV_API
int H420ToRGB565(const uint8_t* src_y,
                 int src_stride_y,
                 const uint8_t* src_u,
                 int src_stride_u,
                 const uint8_t* src_v,
                 int src_stride_v,
                 uint8_t* dst_rgb565,
                 int dst_stride_rgb565,
                 int width,
                 int height);

LIBYUV_API
int I422ToRGB565(const uint8_t* src_y,
                 int src_stride_y,
                 const uint8_t* src_u,
                 int src_stride_u,
                 const uint8_t* src_v,
                 int src_stride_v,
                 uint8_t* dst_rgb565,
                 int dst_stride_rgb565,
                 int width,
                 int height);

LIBYUV_API
int I420ToRGB565Dither(const uint8_t* src_y,
                       int src_stride_y,
                       const uint8_t* src_u,
                       int src_stride_u,
                       const uint8_t* src_v,
                       int src_stride_v,
                       uint8_t* dst_rgb565,
                       int dst_stride_rgb565,
                       const uint8_t* dither4x4,
                       int width,
                       int height);

LIBYUV_API
int I420ToARGB1555(const uint8_t* src_y,
                   int src_stride_y,
                   const uint8_t* src_u,
                   int src_stride_u,
                   const uint8_t* src_v,
                   int src_stride_v,
                   uint8_t* dst_argb1555,
                   int dst_stride_argb1555,
                   int width,
                   int height);

LIBYUV_API
int I420ToARGB4444(const uint8_t* src_y,
                   int src_stride_y,
                   const uint8_t* src_u,
                   int src_stride_u,
                   const uint8_t* src_v,
                   int src_stride_v,
                   uint8_t* dst_argb4444,
                   int dst_stride_argb4444,
                   int width,
                   int height);

LIBYUV_API
int I420ToAR30(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_ar30,
               int dst_stride_ar30,
               int width,
               int height);

LIBYUV_API
int H420ToAR30(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_ar30,
               int dst_stride_ar30,
               int width,
               int height);

LIBYUV_API
int ConvertFromI420(const uint8_t* y,
                    int y_stride,
                    const uint8_t* u,
                    int u_stride,
                    const uint8_t* v,
                    int v_stride,
                    uint8_t* dst_sample,
                    int dst_sample_stride,
                    int width,
                    int height,
                    uint32_t fourcc);

#ifdef __cplusplus
}
}
#endif

#endif

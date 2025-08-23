

#ifndef INCLUDE_LIBYUV_PLANAR_FUNCTIONS_H_
#define INCLUDE_LIBYUV_PLANAR_FUNCTIONS_H_

#include "libyuv/basic_types.h"

#include "libyuv/convert.h"
#include "libyuv/convert_argb.h"

#ifdef __cplusplus
namespace libyuv {
extern "C" {
#endif

#if defined(__pnacl__) || defined(__CLR_VER) ||            \
    (defined(__native_client__) && defined(__x86_64__)) || \
    (defined(__i386__) && !defined(__SSE__) && !defined(__clang__))
#define LIBYUV_DISABLE_X86
#endif
#if defined(__has_feature)
#if __has_feature(memory_sanitizer)
#define LIBYUV_DISABLE_X86
#endif
#endif
#if !defined(LIBYUV_DISABLE_X86) && \
    (defined(_M_IX86) || defined(__x86_64__) || defined(__i386__))
#define HAS_ARGBAFFINEROW_SSE2
#endif

LIBYUV_API
void CopyPlane(const uint8_t* src_y,
               int src_stride_y,
               uint8_t* dst_y,
               int dst_stride_y,
               int width,
               int height);

LIBYUV_API
void CopyPlane_16(const uint16_t* src_y,
                  int src_stride_y,
                  uint16_t* dst_y,
                  int dst_stride_y,
                  int width,
                  int height);

LIBYUV_API
void Convert16To8Plane(const uint16_t* src_y,
                       int src_stride_y,
                       uint8_t* dst_y,
                       int dst_stride_y,
                       int scale,
                       int width,
                       int height);

LIBYUV_API
void Convert8To16Plane(const uint8_t* src_y,
                       int src_stride_y,
                       uint16_t* dst_y,
                       int dst_stride_y,
                       int scale,
                       int width,
                       int height);

LIBYUV_API
void SetPlane(uint8_t* dst_y,
              int dst_stride_y,
              int width,
              int height,
              uint32_t value);

LIBYUV_API
void SplitUVPlane(const uint8_t* src_uv,
                  int src_stride_uv,
                  uint8_t* dst_u,
                  int dst_stride_u,
                  uint8_t* dst_v,
                  int dst_stride_v,
                  int width,
                  int height);

LIBYUV_API
void MergeUVPlane(const uint8_t* src_u,
                  int src_stride_u,
                  const uint8_t* src_v,
                  int src_stride_v,
                  uint8_t* dst_uv,
                  int dst_stride_uv,
                  int width,
                  int height);

LIBYUV_API
void SplitRGBPlane(const uint8_t* src_rgb,
                   int src_stride_rgb,
                   uint8_t* dst_r,
                   int dst_stride_r,
                   uint8_t* dst_g,
                   int dst_stride_g,
                   uint8_t* dst_b,
                   int dst_stride_b,
                   int width,
                   int height);

LIBYUV_API
void MergeRGBPlane(const uint8_t* src_r,
                   int src_stride_r,
                   const uint8_t* src_g,
                   int src_stride_g,
                   const uint8_t* src_b,
                   int src_stride_b,
                   uint8_t* dst_rgb,
                   int dst_stride_rgb,
                   int width,
                   int height);

LIBYUV_API
int I400ToI400(const uint8_t* src_y,
               int src_stride_y,
               uint8_t* dst_y,
               int dst_stride_y,
               int width,
               int height);

#define J400ToJ400 I400ToI400

#define I422ToI422 I422Copy
LIBYUV_API
int I422Copy(const uint8_t* src_y,
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

#define I444ToI444 I444Copy
LIBYUV_API
int I444Copy(const uint8_t* src_y,
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
int YUY2ToI422(const uint8_t* src_yuy2,
               int src_stride_yuy2,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_u,
               int dst_stride_u,
               uint8_t* dst_v,
               int dst_stride_v,
               int width,
               int height);

LIBYUV_API
int UYVYToI422(const uint8_t* src_uyvy,
               int src_stride_uyvy,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_u,
               int dst_stride_u,
               uint8_t* dst_v,
               int dst_stride_v,
               int width,
               int height);

LIBYUV_API
int YUY2ToNV12(const uint8_t* src_yuy2,
               int src_stride_yuy2,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_uv,
               int dst_stride_uv,
               int width,
               int height);

LIBYUV_API
int UYVYToNV12(const uint8_t* src_uyvy,
               int src_stride_uyvy,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_uv,
               int dst_stride_uv,
               int width,
               int height);

LIBYUV_API
int NV21ToNV12(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_vu,
               int src_stride_vu,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_uv,
               int dst_stride_uv,
               int width,
               int height);

LIBYUV_API
int YUY2ToY(const uint8_t* src_yuy2,
            int src_stride_yuy2,
            uint8_t* dst_y,
            int dst_stride_y,
            int width,
            int height);

LIBYUV_API
int I420ToI400(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_y,
               int dst_stride_y,
               int width,
               int height);

#define J420ToJ400 I420ToI400
#define I420ToI420Mirror I420Mirror

LIBYUV_API
int I420Mirror(const uint8_t* src_y,
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

#define I400ToI400Mirror I400Mirror

LIBYUV_API
int I400Mirror(const uint8_t* src_y,
               int src_stride_y,
               uint8_t* dst_y,
               int dst_stride_y,
               int width,
               int height);

#define ARGBToARGBMirror ARGBMirror

LIBYUV_API
int ARGBMirror(const uint8_t* src_argb,
               int src_stride_argb,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

LIBYUV_API
int NV12ToRGB565(const uint8_t* src_y,
                 int src_stride_y,
                 const uint8_t* src_uv,
                 int src_stride_uv,
                 uint8_t* dst_rgb565,
                 int dst_stride_rgb565,
                 int width,
                 int height);

LIBYUV_API
int I422ToBGRA(const uint8_t* src_y,
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
int I422ToABGR(const uint8_t* src_y,
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
int I422ToRGBA(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_rgba,
               int dst_stride_rgba,
               int width,
               int height);

#define RGB24ToRAW RAWToRGB24

LIBYUV_API
int RAWToRGB24(const uint8_t* src_raw,
               int src_stride_raw,
               uint8_t* dst_rgb24,
               int dst_stride_rgb24,
               int width,
               int height);

LIBYUV_API
int I420Rect(uint8_t* dst_y,
             int dst_stride_y,
             uint8_t* dst_u,
             int dst_stride_u,
             uint8_t* dst_v,
             int dst_stride_v,
             int x,
             int y,
             int width,
             int height,
             int value_y,
             int value_u,
             int value_v);

LIBYUV_API
int ARGBRect(uint8_t* dst_argb,
             int dst_stride_argb,
             int dst_x,
             int dst_y,
             int width,
             int height,
             uint32_t value);

LIBYUV_API
int ARGBGrayTo(const uint8_t* src_argb,
               int src_stride_argb,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

LIBYUV_API
int ARGBGray(uint8_t* dst_argb,
             int dst_stride_argb,
             int dst_x,
             int dst_y,
             int width,
             int height);

LIBYUV_API
int ARGBSepia(uint8_t* dst_argb,
              int dst_stride_argb,
              int dst_x,
              int dst_y,
              int width,
              int height);

LIBYUV_API
int ARGBColorMatrix(const uint8_t* src_argb,
                    int src_stride_argb,
                    uint8_t* dst_argb,
                    int dst_stride_argb,
                    const int8_t* matrix_argb,
                    int width,
                    int height);

LIBYUV_API
int RGBColorMatrix(uint8_t* dst_argb,
                   int dst_stride_argb,
                   const int8_t* matrix_rgb,
                   int dst_x,
                   int dst_y,
                   int width,
                   int height);

LIBYUV_API
int ARGBColorTable(uint8_t* dst_argb,
                   int dst_stride_argb,
                   const uint8_t* table_argb,
                   int dst_x,
                   int dst_y,
                   int width,
                   int height);

LIBYUV_API
int RGBColorTable(uint8_t* dst_argb,
                  int dst_stride_argb,
                  const uint8_t* table_argb,
                  int dst_x,
                  int dst_y,
                  int width,
                  int height);

LIBYUV_API
int ARGBLumaColorTable(const uint8_t* src_argb,
                       int src_stride_argb,
                       uint8_t* dst_argb,
                       int dst_stride_argb,
                       const uint8_t* luma,
                       int width,
                       int height);

LIBYUV_API
int ARGBPolynomial(const uint8_t* src_argb,
                   int src_stride_argb,
                   uint8_t* dst_argb,
                   int dst_stride_argb,
                   const float* poly,
                   int width,
                   int height);

LIBYUV_API
int HalfFloatPlane(const uint16_t* src_y,
                   int src_stride_y,
                   uint16_t* dst_y,
                   int dst_stride_y,
                   float scale,
                   int width,
                   int height);

LIBYUV_API
int ByteToFloat(const uint8_t* src_y, float* dst_y, float scale, int width);

LIBYUV_API
int ARGBQuantize(uint8_t* dst_argb,
                 int dst_stride_argb,
                 int scale,
                 int interval_size,
                 int interval_offset,
                 int dst_x,
                 int dst_y,
                 int width,
                 int height);

LIBYUV_API
int ARGBCopy(const uint8_t* src_argb,
             int src_stride_argb,
             uint8_t* dst_argb,
             int dst_stride_argb,
             int width,
             int height);

LIBYUV_API
int ARGBCopyAlpha(const uint8_t* src_argb,
                  int src_stride_argb,
                  uint8_t* dst_argb,
                  int dst_stride_argb,
                  int width,
                  int height);

LIBYUV_API
int ARGBExtractAlpha(const uint8_t* src_argb,
                     int src_stride_argb,
                     uint8_t* dst_a,
                     int dst_stride_a,
                     int width,
                     int height);

LIBYUV_API
int ARGBCopyYToAlpha(const uint8_t* src_y,
                     int src_stride_y,
                     uint8_t* dst_argb,
                     int dst_stride_argb,
                     int width,
                     int height);

typedef void (*ARGBBlendRow)(const uint8_t* src_argb0,
                             const uint8_t* src_argb1,
                             uint8_t* dst_argb,
                             int width);

LIBYUV_API
ARGBBlendRow GetARGBBlend();

LIBYUV_API
int ARGBBlend(const uint8_t* src_argb0,
              int src_stride_argb0,
              const uint8_t* src_argb1,
              int src_stride_argb1,
              uint8_t* dst_argb,
              int dst_stride_argb,
              int width,
              int height);

LIBYUV_API
int BlendPlane(const uint8_t* src_y0,
               int src_stride_y0,
               const uint8_t* src_y1,
               int src_stride_y1,
               const uint8_t* alpha,
               int alpha_stride,
               uint8_t* dst_y,
               int dst_stride_y,
               int width,
               int height);

LIBYUV_API
int I420Blend(const uint8_t* src_y0,
              int src_stride_y0,
              const uint8_t* src_u0,
              int src_stride_u0,
              const uint8_t* src_v0,
              int src_stride_v0,
              const uint8_t* src_y1,
              int src_stride_y1,
              const uint8_t* src_u1,
              int src_stride_u1,
              const uint8_t* src_v1,
              int src_stride_v1,
              const uint8_t* alpha,
              int alpha_stride,
              uint8_t* dst_y,
              int dst_stride_y,
              uint8_t* dst_u,
              int dst_stride_u,
              uint8_t* dst_v,
              int dst_stride_v,
              int width,
              int height);

LIBYUV_API
int ARGBMultiply(const uint8_t* src_argb0,
                 int src_stride_argb0,
                 const uint8_t* src_argb1,
                 int src_stride_argb1,
                 uint8_t* dst_argb,
                 int dst_stride_argb,
                 int width,
                 int height);

LIBYUV_API
int ARGBAdd(const uint8_t* src_argb0,
            int src_stride_argb0,
            const uint8_t* src_argb1,
            int src_stride_argb1,
            uint8_t* dst_argb,
            int dst_stride_argb,
            int width,
            int height);

LIBYUV_API
int ARGBSubtract(const uint8_t* src_argb0,
                 int src_stride_argb0,
                 const uint8_t* src_argb1,
                 int src_stride_argb1,
                 uint8_t* dst_argb,
                 int dst_stride_argb,
                 int width,
                 int height);

LIBYUV_API
int I422ToYUY2(const uint8_t* src_y,
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
int I422ToUYVY(const uint8_t* src_y,
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
int ARGBAttenuate(const uint8_t* src_argb,
                  int src_stride_argb,
                  uint8_t* dst_argb,
                  int dst_stride_argb,
                  int width,
                  int height);

LIBYUV_API
int ARGBUnattenuate(const uint8_t* src_argb,
                    int src_stride_argb,
                    uint8_t* dst_argb,
                    int dst_stride_argb,
                    int width,
                    int height);

LIBYUV_API
int ARGBComputeCumulativeSum(const uint8_t* src_argb,
                             int src_stride_argb,
                             int32_t* dst_cumsum,
                             int dst_stride32_cumsum,
                             int width,
                             int height);

LIBYUV_API
int ARGBBlur(const uint8_t* src_argb,
             int src_stride_argb,
             uint8_t* dst_argb,
             int dst_stride_argb,
             int32_t* dst_cumsum,
             int dst_stride32_cumsum,
             int width,
             int height,
             int radius);

LIBYUV_API
int ARGBShade(const uint8_t* src_argb,
              int src_stride_argb,
              uint8_t* dst_argb,
              int dst_stride_argb,
              int width,
              int height,
              uint32_t value);

LIBYUV_API
int InterpolatePlane(const uint8_t* src0,
                     int src_stride0,
                     const uint8_t* src1,
                     int src_stride1,
                     uint8_t* dst,
                     int dst_stride,
                     int width,
                     int height,
                     int interpolation);

LIBYUV_API
int ARGBInterpolate(const uint8_t* src_argb0,
                    int src_stride_argb0,
                    const uint8_t* src_argb1,
                    int src_stride_argb1,
                    uint8_t* dst_argb,
                    int dst_stride_argb,
                    int width,
                    int height,
                    int interpolation);

LIBYUV_API
int I420Interpolate(const uint8_t* src0_y,
                    int src0_stride_y,
                    const uint8_t* src0_u,
                    int src0_stride_u,
                    const uint8_t* src0_v,
                    int src0_stride_v,
                    const uint8_t* src1_y,
                    int src1_stride_y,
                    const uint8_t* src1_u,
                    int src1_stride_u,
                    const uint8_t* src1_v,
                    int src1_stride_v,
                    uint8_t* dst_y,
                    int dst_stride_y,
                    uint8_t* dst_u,
                    int dst_stride_u,
                    uint8_t* dst_v,
                    int dst_stride_v,
                    int width,
                    int height,
                    int interpolation);

LIBYUV_API
void ARGBAffineRow_C(const uint8_t* src_argb,
                     int src_argb_stride,
                     uint8_t* dst_argb,
                     const float* uv_dudv,
                     int width);
LIBYUV_API
void ARGBAffineRow_SSE2(const uint8_t* src_argb,
                        int src_argb_stride,
                        uint8_t* dst_argb,
                        const float* uv_dudv,
                        int width);

LIBYUV_API
int ARGBShuffle(const uint8_t* src_bgra,
                int src_stride_bgra,
                uint8_t* dst_argb,
                int dst_stride_argb,
                const uint8_t* shuffler,
                int width,
                int height);

LIBYUV_API
int ARGBSobelToPlane(const uint8_t* src_argb,
                     int src_stride_argb,
                     uint8_t* dst_y,
                     int dst_stride_y,
                     int width,
                     int height);

LIBYUV_API
int ARGBSobel(const uint8_t* src_argb,
              int src_stride_argb,
              uint8_t* dst_argb,
              int dst_stride_argb,
              int width,
              int height);

LIBYUV_API
int ARGBSobelXY(const uint8_t* src_argb,
                int src_stride_argb,
                uint8_t* dst_argb,
                int dst_stride_argb,
                int width,
                int height);

#ifdef __cplusplus
}
}
#endif

#endif

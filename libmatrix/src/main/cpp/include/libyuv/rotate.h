

#ifndef INCLUDE_LIBYUV_ROTATE_H_
#define INCLUDE_LIBYUV_ROTATE_H_

#include "libyuv/basic_types.h"

#ifdef __cplusplus
namespace libyuv {
extern "C" {
#endif

typedef enum RotationMode {
  kRotate0 = 0,
  kRotate90 = 90,
  kRotate180 = 180,
  kRotate270 = 270,

  kRotateNone = 0,
  kRotateClockwise = 90,
  kRotateCounterClockwise = 270,
} RotationModeEnum;

LIBYUV_API
int I420Rotate(const uint8_t* src_y,
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
               int height,
               enum RotationMode mode);

LIBYUV_API
int NV12ToI420Rotate(const uint8_t* src_y,
                     int src_stride_y,
                     const uint8_t* src_uv,
                     int src_stride_uv,
                     uint8_t* dst_y,
                     int dst_stride_y,
                     uint8_t* dst_u,
                     int dst_stride_u,
                     uint8_t* dst_v,
                     int dst_stride_v,
                     int width,
                     int height,
                     enum RotationMode mode);

LIBYUV_API
int RotatePlane(const uint8_t* src,
                int src_stride,
                uint8_t* dst,
                int dst_stride,
                int width,
                int height,
                enum RotationMode mode);

LIBYUV_API
void RotatePlane90(const uint8_t* src,
                   int src_stride,
                   uint8_t* dst,
                   int dst_stride,
                   int width,
                   int height);

LIBYUV_API
void RotatePlane180(const uint8_t* src,
                    int src_stride,
                    uint8_t* dst,
                    int dst_stride,
                    int width,
                    int height);

LIBYUV_API
void RotatePlane270(const uint8_t* src,
                    int src_stride,
                    uint8_t* dst,
                    int dst_stride,
                    int width,
                    int height);

LIBYUV_API
void RotateUV90(const uint8_t* src,
                int src_stride,
                uint8_t* dst_a,
                int dst_stride_a,
                uint8_t* dst_b,
                int dst_stride_b,
                int width,
                int height);

LIBYUV_API
void RotateUV180(const uint8_t* src,
                 int src_stride,
                 uint8_t* dst_a,
                 int dst_stride_a,
                 uint8_t* dst_b,
                 int dst_stride_b,
                 int width,
                 int height);

LIBYUV_API
void RotateUV270(const uint8_t* src,
                 int src_stride,
                 uint8_t* dst_a,
                 int dst_stride_a,
                 uint8_t* dst_b,
                 int dst_stride_b,
                 int width,
                 int height);

LIBYUV_API
void TransposePlane(const uint8_t* src,
                    int src_stride,
                    uint8_t* dst,
                    int dst_stride,
                    int width,
                    int height);

LIBYUV_API
void TransposeUV(const uint8_t* src,
                 int src_stride,
                 uint8_t* dst_a,
                 int dst_stride_a,
                 uint8_t* dst_b,
                 int dst_stride_b,
                 int width,
                 int height);

#ifdef __cplusplus
}
}
#endif

#endif



#ifndef INCLUDE_LIBYUV_SCALE_ARGB_H_
#define INCLUDE_LIBYUV_SCALE_ARGB_H_

#include "libyuv/basic_types.h"
#include "libyuv/scale.h"

#ifdef __cplusplus
namespace libyuv {
extern "C" {
#endif

LIBYUV_API
int ARGBScale(const uint8_t* src_argb,
              int src_stride_argb,
              int src_width,
              int src_height,
              uint8_t* dst_argb,
              int dst_stride_argb,
              int dst_width,
              int dst_height,
              enum FilterMode filtering);

LIBYUV_API
int ARGBScaleClip(const uint8_t* src_argb,
                  int src_stride_argb,
                  int src_width,
                  int src_height,
                  uint8_t* dst_argb,
                  int dst_stride_argb,
                  int dst_width,
                  int dst_height,
                  int clip_x,
                  int clip_y,
                  int clip_width,
                  int clip_height,
                  enum FilterMode filtering);

LIBYUV_API
int YUVToARGBScaleClip(const uint8_t* src_y,
                       int src_stride_y,
                       const uint8_t* src_u,
                       int src_stride_u,
                       const uint8_t* src_v,
                       int src_stride_v,
                       uint32_t src_fourcc,
                       int src_width,
                       int src_height,
                       uint8_t* dst_argb,
                       int dst_stride_argb,
                       uint32_t dst_fourcc,
                       int dst_width,
                       int dst_height,
                       int clip_x,
                       int clip_y,
                       int clip_width,
                       int clip_height,
                       enum FilterMode filtering);

#ifdef __cplusplus
}
}
#endif

#endif

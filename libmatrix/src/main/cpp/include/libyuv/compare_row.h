

#ifndef INCLUDE_LIBYUV_COMPARE_ROW_H_
#define INCLUDE_LIBYUV_COMPARE_ROW_H_

#include "libyuv/basic_types.h"

#ifdef __cplusplus
namespace libyuv {
extern "C" {
#endif

#if defined(__pnacl__) || defined(__CLR_VER) ||            \
    (defined(__native_client__) && defined(__x86_64__)) || \
    (defined(__i386__) && !defined(__SSE__) && !defined(__clang__))
#define LIBYUV_DISABLE_X86
#endif
#if defined(__native_client__)
#define LIBYUV_DISABLE_NEON
#endif
#if defined(__has_feature)
#if __has_feature(memory_sanitizer)
#define LIBYUV_DISABLE_X86
#endif
#endif
#if defined(_M_IX86) && !defined(__clang__) && defined(_MSC_VER) && \
    _MSC_VER >= 1700
#define VISUALC_HAS_AVX2 1
#endif

#if defined(__clang__) && (defined(__x86_64__) || defined(__i386__))
#if (__clang_major__ > 3) || (__clang_major__ == 3 && (__clang_minor__ >= 4))
#define CLANG_HAS_AVX2 1
#endif
#endif

#if !defined(LIBYUV_DISABLE_X86) && \
    (defined(__x86_64__) || defined(__i386__) || defined(_M_IX86))
#define HAS_HASHDJB2_SSE41
#define HAS_SUMSQUAREERROR_SSE2
#define HAS_HAMMINGDISTANCE_SSE42
#endif

#if !defined(LIBYUV_DISABLE_X86) && defined(_M_IX86) && defined(_MSC_VER) && \
    (defined(VISUALC_HAS_AVX2) || defined(CLANG_HAS_AVX2))
#define HAS_HASHDJB2_AVX2
#define HAS_SUMSQUAREERROR_AVX2
#endif

#if !defined(LIBYUV_DISABLE_X86) && \
    (defined(__x86_64__) || (defined(__i386__) && !defined(_MSC_VER)))
#define HAS_HAMMINGDISTANCE_SSSE3
#endif

#if !defined(LIBYUV_DISABLE_X86) && defined(CLANG_HAS_AVX2) && \
    (defined(__x86_64__) || (defined(__i386__) && !defined(_MSC_VER)))
#define HAS_HAMMINGDISTANCE_AVX2
#endif

#if !defined(LIBYUV_DISABLE_NEON) && \
    (defined(__ARM_NEON__) || defined(LIBYUV_NEON) || defined(__aarch64__))
#define HAS_SUMSQUAREERROR_NEON
#define HAS_HAMMINGDISTANCE_NEON
#endif

#if !defined(LIBYUV_DISABLE_MSA) && defined(__mips_msa)
#define HAS_HAMMINGDISTANCE_MSA
#define HAS_SUMSQUAREERROR_MSA
#endif

#if !defined(LIBYUV_DISABLE_MMI) && defined(_MIPS_ARCH_LOONGSON3A)
#define HAS_HAMMINGDISTANCE_MMI
#define HAS_SUMSQUAREERROR_MMI
#endif

uint32_t HammingDistance_C(const uint8_t* src_a,
                           const uint8_t* src_b,
                           int count);
uint32_t HammingDistance_SSE42(const uint8_t* src_a,
                               const uint8_t* src_b,
                               int count);
uint32_t HammingDistance_SSSE3(const uint8_t* src_a,
                               const uint8_t* src_b,
                               int count);
uint32_t HammingDistance_AVX2(const uint8_t* src_a,
                              const uint8_t* src_b,
                              int count);
uint32_t HammingDistance_NEON(const uint8_t* src_a,
                              const uint8_t* src_b,
                              int count);
uint32_t HammingDistance_MSA(const uint8_t* src_a,
                             const uint8_t* src_b,
                             int count);
uint32_t HammingDistance_MMI(const uint8_t* src_a,
                             const uint8_t* src_b,
                             int count);
uint32_t SumSquareError_C(const uint8_t* src_a,
                          const uint8_t* src_b,
                          int count);
uint32_t SumSquareError_SSE2(const uint8_t* src_a,
                             const uint8_t* src_b,
                             int count);
uint32_t SumSquareError_AVX2(const uint8_t* src_a,
                             const uint8_t* src_b,
                             int count);
uint32_t SumSquareError_NEON(const uint8_t* src_a,
                             const uint8_t* src_b,
                             int count);
uint32_t SumSquareError_MSA(const uint8_t* src_a,
                            const uint8_t* src_b,
                            int count);
uint32_t SumSquareError_MMI(const uint8_t* src_a,
                            const uint8_t* src_b,
                            int count);

uint32_t HashDjb2_C(const uint8_t* src, int count, uint32_t seed);
uint32_t HashDjb2_SSE41(const uint8_t* src, int count, uint32_t seed);
uint32_t HashDjb2_AVX2(const uint8_t* src, int count, uint32_t seed);

#ifdef __cplusplus
}
}
#endif

#endif

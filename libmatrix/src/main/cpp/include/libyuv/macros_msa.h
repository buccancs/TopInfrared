

#ifndef INCLUDE_LIBYUV_MACROS_MSA_H_
#define INCLUDE_LIBYUV_MACROS_MSA_H_

#if !defined(LIBYUV_DISABLE_MSA) && defined(__mips_msa)
#include <msa.h>
#include <stdint.h>

#if (__mips_isa_rev >= 6)
#define LW(psrc)                                       \
  ({                                                   \
    const uint8_t* psrc_lw_m = (const uint8_t*)(psrc); \
    uint32_t val_m;                                    \
    asm volatile("lw  %[val_m],  %[psrc_lw_m]  \n"     \
                 : [val_m] "=r"(val_m)                 \
                 : [psrc_lw_m] "m"(*psrc_lw_m));       \
    val_m;                                             \
  })

#if (__mips == 64)
#define LD(psrc)                                       \
  ({                                                   \
    const uint8_t* psrc_ld_m = (const uint8_t*)(psrc); \
    uint64_t val_m = 0;                                \
    asm volatile("ld  %[val_m],  %[psrc_ld_m]  \n"     \
                 : [val_m] "=r"(val_m)                 \
                 : [psrc_ld_m] "m"(*psrc_ld_m));       \
    val_m;                                             \
  })
#else
#define LD(psrc)                                                         \
  ({                                                                     \
    const uint8_t* psrc_ld_m = (const uint8_t*)(psrc);                   \
    uint32_t val0_m, val1_m;                                             \
    uint64_t val_m = 0;                                                  \
    val0_m = LW(psrc_ld_m);                                              \
    val1_m = LW(psrc_ld_m + 4);                                          \
    val_m = (uint64_t)(val1_m);                              \
    val_m = (uint64_t)((val_m << 32) & 0xFFFFFFFF00000000);  \
    val_m = (uint64_t)(val_m | (uint64_t)val0_m);            \
    val_m;                                                               \
  })
#endif

#define SW(val, pdst)                                   \
  ({                                                    \
    uint8_t* pdst_sw_m = (uint8_t*)(pdst);  \
    uint32_t val_m = (val);                             \
    asm volatile("sw  %[val_m],  %[pdst_sw_m]  \n"      \
                 : [pdst_sw_m] "=m"(*pdst_sw_m)         \
                 : [val_m] "r"(val_m));                 \
  })

#if (__mips == 64)
#define SD(val, pdst)                                   \
  ({                                                    \
    uint8_t* pdst_sd_m = (uint8_t*)(pdst);  \
    uint64_t val_m = (val);                             \
    asm volatile("sd  %[val_m],  %[pdst_sd_m]  \n"      \
                 : [pdst_sd_m] "=m"(*pdst_sd_m)         \
                 : [val_m] "r"(val_m));                 \
  })
#else
#define SD(val, pdst)                                        \
  ({                                                         \
    uint8_t* pdst_sd_m = (uint8_t*)(pdst);       \
    uint32_t val0_m, val1_m;                                 \
    val0_m = (uint32_t)((val)&0x00000000FFFFFFFF);           \
    val1_m = (uint32_t)(((val) >> 32) & 0x00000000FFFFFFFF); \
    SW(val0_m, pdst_sd_m);                                   \
    SW(val1_m, pdst_sd_m + 4);                               \
  })
#endif
#else
#define LW(psrc)                                       \
  ({                                                   \
    const uint8_t* psrc_lw_m = (const uint8_t*)(psrc); \
    uint32_t val_m;                                    \
    asm volatile("ulw  %[val_m],  %[psrc_lw_m]  \n"    \
                 : [val_m] "=r"(val_m)                 \
                 : [psrc_lw_m] "m"(*psrc_lw_m));       \
    val_m;                                             \
  })

#if (__mips == 64)
#define LD(psrc)                                       \
  ({                                                   \
    const uint8_t* psrc_ld_m = (const uint8_t*)(psrc); \
    uint64_t val_m = 0;                                \
    asm volatile("uld  %[val_m],  %[psrc_ld_m]  \n"    \
                 : [val_m] "=r"(val_m)                 \
                 : [psrc_ld_m] "m"(*psrc_ld_m));       \
    val_m;                                             \
  })
#else
#define LD(psrc)                                                         \
  ({                                                                     \
    const uint8_t* psrc_ld_m = (const uint8_t*)(psrc);                   \
    uint32_t val0_m, val1_m;                                             \
    uint64_t val_m = 0;                                                  \
    val0_m = LW(psrc_ld_m);                                              \
    val1_m = LW(psrc_ld_m + 4);                                          \
    val_m = (uint64_t)(val1_m);                              \
    val_m = (uint64_t)((val_m << 32) & 0xFFFFFFFF00000000);  \
    val_m = (uint64_t)(val_m | (uint64_t)val0_m);            \
    val_m;                                                               \
  })
#endif

#define SW(val, pdst)                                   \
  ({                                                    \
    uint8_t* pdst_sw_m = (uint8_t*)(pdst);  \
    uint32_t val_m = (val);                             \
    asm volatile("usw  %[val_m],  %[pdst_sw_m]  \n"     \
                 : [pdst_sw_m] "=m"(*pdst_sw_m)         \
                 : [val_m] "r"(val_m));                 \
  })

#define SD(val, pdst)                                        \
  ({                                                         \
    uint8_t* pdst_sd_m = (uint8_t*)(pdst);       \
    uint32_t val0_m, val1_m;                                 \
    val0_m = (uint32_t)((val)&0x00000000FFFFFFFF);           \
    val1_m = (uint32_t)(((val) >> 32) & 0x00000000FFFFFFFF); \
    SW(val0_m, pdst_sd_m);                                   \
    SW(val1_m, pdst_sd_m + 4);                               \
  })
#endif

#define LD_B(RTYPE, psrc) *((RTYPE*)(psrc)) 
#define LD_UB(...) LD_B(const v16u8, __VA_ARGS__)

#define ST_B(RTYPE, in, pdst) *((RTYPE*)(pdst)) = (in) 
#define ST_UB(...) ST_B(v16u8, __VA_ARGS__)

#define ST_H(RTYPE, in, pdst) *((RTYPE*)(pdst)) = (in) 
#define ST_UH(...) ST_H(v8u16, __VA_ARGS__)

#define LD_B2(RTYPE, psrc, stride, out0, out1) \
  {                                            \
    out0 = LD_B(RTYPE, (psrc));                \
    out1 = LD_B(RTYPE, (psrc) + stride);       \
  }
#define LD_UB2(...) LD_B2(const v16u8, __VA_ARGS__)

#define LD_B4(RTYPE, psrc, stride, out0, out1, out2, out3) \
  {                                                        \
    LD_B2(RTYPE, (psrc), stride, out0, out1);              \
    LD_B2(RTYPE, (psrc) + 2 * stride, stride, out2, out3); \
  }
#define LD_UB4(...) LD_B4(const v16u8, __VA_ARGS__)

#define ST_B2(RTYPE, in0, in1, pdst, stride) \
  {                                          \
    ST_B(RTYPE, in0, (pdst));                \
    ST_B(RTYPE, in1, (pdst) + stride);       \
  }
#define ST_UB2(...) ST_B2(v16u8, __VA_ARGS__)

#define ST_B4(RTYPE, in0, in1, in2, in3, pdst, stride)   \
  {                                                      \
    ST_B2(RTYPE, in0, in1, (pdst), stride);              \
    ST_B2(RTYPE, in2, in3, (pdst) + 2 * stride, stride); \
  }
#define ST_UB4(...) ST_B4(v16u8, __VA_ARGS__)

#define ST_H2(RTYPE, in0, in1, pdst, stride) \
  {                                          \
    ST_H(RTYPE, in0, (pdst));                \
    ST_H(RTYPE, in1, (pdst) + stride);       \
  }
#define ST_UH2(...) ST_H2(v8u16, __VA_ARGS__)

#define VSHF_B2(RTYPE, in0, in1, in2, in3, mask0, mask1, out0, out1)  \
  {                                                                   \
    out0 = (RTYPE)__msa_vshf_b((v16i8)mask0, (v16i8)in1, (v16i8)in0); \
    out1 = (RTYPE)__msa_vshf_b((v16i8)mask1, (v16i8)in3, (v16i8)in2); \
  }
#define VSHF_B2_UB(...) VSHF_B2(v16u8, __VA_ARGS__)

#define ILVRL_B2(RTYPE, in0, in1, out0, out1)           \
  {                                                     \
    out0 = (RTYPE)__msa_ilvr_b((v16i8)in0, (v16i8)in1); \
    out1 = (RTYPE)__msa_ilvl_b((v16i8)in0, (v16i8)in1); \
  }
#define ILVRL_B2_UB(...) ILVRL_B2(v16u8, __VA_ARGS__)

#endif 

#endif

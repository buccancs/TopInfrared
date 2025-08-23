

#ifndef INCLUDE_LIBYUV_CPU_ID_H_
#define INCLUDE_LIBYUV_CPU_ID_H_

#include "libyuv/basic_types.h"

#ifdef __cplusplus
namespace libyuv {
extern "C" {
#endif

static const int kCpuInitialized = 0x1;

static const int kCpuHasARM = 0x2;
static const int kCpuHasNEON = 0x4;

static const int kCpuHasX86 = 0x10;
static const int kCpuHasSSE2 = 0x20;
static const int kCpuHasSSSE3 = 0x40;
static const int kCpuHasSSE41 = 0x80;
static const int kCpuHasSSE42 = 0x100;
static const int kCpuHasAVX = 0x200;
static const int kCpuHasAVX2 = 0x400;
static const int kCpuHasERMS = 0x800;
static const int kCpuHasFMA3 = 0x1000;
static const int kCpuHasF16C = 0x2000;
static const int kCpuHasGFNI = 0x4000;
static const int kCpuHasAVX512BW = 0x8000;
static const int kCpuHasAVX512VL = 0x10000;
static const int kCpuHasAVX512VBMI = 0x20000;
static const int kCpuHasAVX512VBMI2 = 0x40000;
static const int kCpuHasAVX512VBITALG = 0x80000;
static const int kCpuHasAVX512VPOPCNTDQ = 0x100000;

static const int kCpuHasMIPS = 0x200000;
static const int kCpuHasMSA = 0x400000;
static const int kCpuHasMMI = 0x800000;

LIBYUV_API
int InitCpuFlags(void);

static __inline int TestCpuFlag(int test_flag) {
  LIBYUV_API extern int cpu_info_;
#ifdef __ATOMIC_RELAXED
  int cpu_info = __atomic_load_n(&cpu_info_, __ATOMIC_RELAXED);
#else
  int cpu_info = cpu_info_;
#endif
  return (!cpu_info ? InitCpuFlags() : cpu_info) & test_flag;
}

LIBYUV_API
int ArmCpuCaps(const char* cpuinfo_name);

LIBYUV_API
int MaskCpuFlags(int enable_flags);

static __inline void SetCpuFlags(int cpu_flags) {
  LIBYUV_API extern int cpu_info_;
#ifdef __ATOMIC_RELAXED
  __atomic_store_n(&cpu_info_, cpu_flags, __ATOMIC_RELAXED);
#else
  cpu_info_ = cpu_flags;
#endif
}

LIBYUV_API
void CpuId(int info_eax, int info_ecx, int* cpu_info);

#ifdef __cplusplus
}
}
#endif

#endif

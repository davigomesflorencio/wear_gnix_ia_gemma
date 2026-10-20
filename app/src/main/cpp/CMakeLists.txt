cmake_minimum_required(VERSION 3.22.1)

project("gnixia-llm" LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# llama.cpp is built as static libraries linked into libgnixia-llm.so, CPU only.
# Everything that isn't needed for on-device inference is turned off.
set(BUILD_SHARED_LIBS     OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON    OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS     OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS     OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES  OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER    OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_APP       OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL         OFF CACHE BOOL "" FORCE)
# Generic build for each ABI: -march=native would target the build machine
set(GGML_NATIVE           OFF CACHE BOOL "" FORCE)
# ggml's own thread pool; avoids shipping libomp
set(GGML_OPENMP           OFF CACHE BOOL "" FORCE)
# llamafile's sgemm needs FP16 NEON intrinsics that generic ARMv7 lacks;
# 32-bit ARM uses ggml's regular CPU kernels instead
if(ANDROID_ABI STREQUAL "armeabi-v7a")
    set(GGML_LLAMAFILE    OFF CACHE BOOL "" FORCE)
endif()

add_subdirectory(llama.cpp build-llama)

add_library(gnixia-llm SHARED gnixia_llm.cpp)

target_link_libraries(gnixia-llm PRIVATE llama android log)

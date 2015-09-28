/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#pragma version(1)
#pragma rs java_package_name(fquintan.renderscripttest)
#pragma rs_fp_relaxed


rs_allocation gIn;

/*Decode yuv into a single channel grayscale*/
void  yuv_to_greyscale_uchar(uchar *v_out, uint32_t x, uint32_t y) {
    uchar yp = rsGetElementAtYuv_uchar_Y(gIn , x, y) & 0xFF;
    *v_out = yp;
}

/*Decode yuv into a 4 channel RGBA grayscale*/
//void  yuv_to_greyscale_uchar4(uchar4 *v_out, uint32_t x, uint32_t y) {
//    uchar yp = rsGetElementAtYuv_uchar_Y(gIn , x, y) & 0xFF;
//
//    uchar4 res4;
//    res4.r = yp;
//    res4.g = yp;
//    res4.b = yp;
//    res4.a = 0xFF;
//    *v_out = res4;
//}
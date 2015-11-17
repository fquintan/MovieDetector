#pragma version(1)
#pragma rs java_package_name(cl.niclabs.moviedetector)
#pragma rs_fp_relaxed

//Kernels
float K1_0_0 = 1;
float K1_0_1 = -1;
float K1_1_0 = 1;
float K1_1_1 = -1;

float K2_0_0 = 1;
float K2_0_1 = 1;
float K2_1_0 = -1;
float K2_1_1 = -1;

float K3_0_0 = M_SQRT2;
float K3_0_1 = 0;
float K3_1_0 = -M_SQRT2;
float K3_1_1 = 0;

float K4_0_0 = 0;
float K4_0_1 = M_SQRT2;
float K4_1_0 = -M_SQRT2;
float K4_1_1 = 0;

float K5_0_0 = 2;
float K5_0_1 = -2;
float K5_1_0 = -2;
float K5_1_1 = 2;


int32_t zoneWidth;
int32_t zoneHeight;
int32_t width;
int32_t height;

volatile int32_t *gOutarray;

rs_allocation gIn;
rs_allocation gOut;
rs_script gScript;

void setup_detector(int32_t n_width, int32_t n_height, int32_t imgWidth, int32_t imgHeight){
    height = n_height;
    width = n_width;
    zoneWidth = imgWidth / n_width;
    zoneHeight = imgHeight / n_height;
}

void root(uchar *v_out, uint32_t x,  uint32_t y) {
    uint32_t x_index = 2 * x;
    uint32_t y_index = 2 * y;

    int A_0_0 = rsGetElementAt_int(gIn, x_index, y_index);
    int A_0_1 = rsGetElementAt_int(gIn, x_index+1, y_index);
    int A_1_0 = rsGetElementAt_int(gIn, x_index, y_index+1);
    int A_1_1 = rsGetElementAt_int(gIn, x_index+1, y_index+1);

    float current = fabs(A_0_0*K1_0_0 + A_0_1*K1_0_1 + A_1_0*K1_1_0 + A_1_1*K1_1_1);
    float max = current;
    uchar max_index = 0;
    current = fabs(A_0_0*K2_0_0 + A_0_1*K2_0_1 + A_1_0*K2_1_0 + A_1_1*K2_1_1);
    if (current > max){
        max = current;
        max_index = 1;
    }
    current = fabs(A_0_0*K3_0_0 + A_0_1*K3_0_1 + A_1_0*K3_1_0 + A_1_1*K3_1_1);
        if (current > max){
            max = current;
            max_index = 2;
    }
    current = fabs(A_0_0*K4_0_0 + A_0_1*K4_0_1 + A_1_0*K4_1_0 + A_1_1*K4_1_1);
        if (current > max){
            max = current;
            max_index = 3;
    }
    current = fabs(A_0_0*K5_0_0 + A_0_1*K5_0_1 + A_1_0*K5_1_0 + A_1_1*K5_1_1);
        if (current > max){
            max = current;
            max_index = 4;
    }
    *v_out = max_index;
}

void compute_reduce() {
    rs_allocation ignoredIn;
    rsForEach(gScript, ignoredIn, gOut);
}

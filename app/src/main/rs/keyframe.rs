#pragma version(1)
#pragma rs java_package_name(cl.niclabs.moviedetector)
#pragma rs_fp_relaxed

int32_t zoneWidth;
int32_t zoneHeight;
int32_t width;
int32_t height;

int32_t r = 0;
int32_t g = 1;
int32_t b = 2;
volatile int32_t *gOutarray;

rs_allocation gIn;
rs_allocation gOut;
rs_script gScript;

void setup2(int32_t n_width, int32_t n_height, int32_t imgWidth, int32_t imgHeight){
    height = n_height;
    width = n_width;
    zoneWidth = imgWidth / n_width;
    zoneHeight = imgHeight / n_height;
}

void root(const uchar4 *v_in, uint32_t x,  uint32_t y) {
    int32_t red = (*v_in).r;
    int32_t green = (*v_in).g;
    int32_t blue = (*v_in).b;
    uchar4 pixel = rsGetElementAt_uchar4(gIn, x, y);
    int32_t red = pixel.r;
    int32_t green = pixel.g;
    int32_t blue = pixel.b;

    int32_t xzone = x / zoneWidth;
    int32_t yzone = y / zoneHeight;
    int32_t index = ((yzone * width) + xzone)*3;
    volatile int32_t* addr_r = gOutarray + index;
    volatile int32_t* addr_g = gOutarray + index + 1;
    volatile int32_t* addr_b = gOutarray + index + 2;
    rsAtomicAdd(addr_r, red);
    rsAtomicAdd(addr_g, green);
    rsAtomicAdd(addr_b, blue);
}


void compute_keyframe() {

    rs_allocation ignoredOut;
    rsForEach(gScript, gIn, ignoredOut);
}

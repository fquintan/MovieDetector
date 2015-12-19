#pragma version(1)
#pragma rs java_package_name(cl.niclabs.moviedetector)
#pragma rs_fp_relaxed

int32_t zoneWidth;
int32_t zoneHeight;
int32_t width;
int32_t height;

volatile int32_t *gOutarray;

rs_allocation gIn;
rs_allocation gOut;
rs_script gScript;

void setup_reducer(int32_t n_width, int32_t n_height, int32_t imgWidth, int32_t imgHeight){
    height = n_height;
    width = n_width;
    zoneWidth = imgWidth / n_width;
    zoneHeight = imgHeight / n_height;
}

void root(const uchar *v_in, uint32_t x,  uint32_t y) {
    int32_t value = *v_in;
    int32_t xzone = x / zoneWidth;
    int32_t yzone = y / zoneHeight;
    int32_t index = (yzone * width) + xzone;
    int index_value = index;
    volatile int32_t* addr = gOutarray + index;
    rsAtomicAdd(addr, value);
}


void compute_reduce() {
    rs_allocation ignoredOut;
    rsForEach(gScript, gIn, ignoredOut);
}

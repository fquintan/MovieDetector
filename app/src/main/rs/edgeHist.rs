#pragma version(1)
#pragma rs java_package_name(cl.niclabs.moviedetector)
#pragma rs_fp_relaxed

int32_t xzones;
int32_t yzones;

int32_t bins = 5;

int32_t zoneWidth;
int32_t zoneHeigth;

volatile int32_t *gOutarray;
rs_allocation gIn;
rs_allocation gOut;
rs_script gScript;

void setup_edge_histogram(int32_t n_xzones, int32_t n_yzones, int32_t imgWidth, int32_t imgHeigth){
    xzones = n_xzones;
    yzones = n_yzones;
    zoneWidth = imgWidth / n_xzones;
    zoneHeigth = imgHeigth / n_yzones;
}

void root(const uchar *v_in, uint32_t x,  uint32_t y) {
    int32_t bin = *v_in;
    int32_t xzone = x / zoneWidth;
    int32_t yzone = y / zoneHeigth;
    int32_t index = (yzone*bins + xzone)*bins + bin;
    volatile int32_t* addr = gOutarray + index;
    rsAtomicInc(addr);
}

void compute_edge_histogram() {

    rs_allocation ignoredOut;
    rsForEach(gScript, gIn, ignoredOut);
}


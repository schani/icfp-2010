
#include <stdio.h>
#include <stdlib.h>

unsigned gateL[3][3] =
	{ { 0, 0 ,0 },
	  { 0, 0 ,0 },
	  { 0, 0 ,0 } };
unsigned gateR[3][3] =
	{ { 0, 0 ,0 },
	  { 0, 0 ,0 },
	  { 0, 0 ,0 } };

unsigned initL = 0;
unsigned initR = 0;


static inline
void calc_gate(unsigned iL, unsigned iR, unsigned *oL, unsigned *oR)
{
	*oL = gateL[iL][iR];
	*oR = gateR[iL][iR];
}



int main(int argc, char *argv[])
{
	unsigned seq[] = { 0, 1, 2, 0, 2, 1, 0, 1, 2, 1, 0, 2, 0, 1, 2, 0, 2 };
	unsigned seq_len = sizeof(seq)/sizeof(seq[0]);

	unsigned id = atoi(argv[1]);
	unsigned total = atoi(argv[2]);

	unsigned long k = 387420489L * id / total;
	unsigned long l = k + 387420489L / total + 1;

	printf("SEQ %lu-%lu\n", k, l);

	for (unsigned long z = k; z < l; z++) {
	    unsigned long v = z;
	    
	    initL = v % 3; v /= 3;
	    initR = v % 3; v /= 3;
	    
	    for (int a = 0; a < 3; a++) {
		for (int b = 0; b < 3; b++) {
		    gateL[a][b] = v % 3; v /= 3;
		    gateR[a][b] = v % 3; v /= 3;
		}
	    }
	
	    unsigned AoL = 0, AoR = initR;
	    unsigned BoL = initL, BoR = 0;
	    unsigned CoL = 0, CoR = initR;
	    unsigned DoL = initL, DoR = 0;
	
	    printf("%u%u=", initL, initR);

	    for (int a = 0; a < 3; a++) {
		for (int b = 0; b < 3; b++) {
		    printf("%u%u.", gateL[a][b], gateR[a][b]);
		}
	    }

	    for (int n = 0; n < seq_len; n++) {
		calc_gate(seq[n], AoR, &AoL, &AoR);
		calc_gate(seq[n], BoL, &BoL, &BoR);
		calc_gate(CoR, seq[n], &CoL, &CoR);
		calc_gate(DoL, seq[n], &DoL, &DoR);

		printf("%u%u%u%u-", AoL, BoR, CoL, DoR);
	    }

	    printf("\n");
	}
}



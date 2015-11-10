/**********************************************************
  Main bbbandroidHAL header file

  Written by Andrew Henderson (hendersa@icculus.org)
  Modified by Ankur Yadav (ankurayadav@gmail.com)

  This code is made available under the BSD license.
**********************************************************/

#include <stdio.h>

#ifndef __BBBANDROIDHAL_H__
#define __BBBANDROIDHAL_H__

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

/* SPI interfacing functions */
extern int spiTransfer(int spiFD, unsigned char tx[], unsigned char rx[], int len);
extern int spiOpen(unsigned int bus, unsigned int device, uint32_t speed, uint8_t mode, uint8_t bpw);
extern unsigned char spiReadByte(int spiFD, unsigned int regAdd);
extern unsigned char* spiReadBytes(int spiFD, unsigned int len, unsigned int startAdd);
extern int spiWriteRegByte(int spiFD, unsigned int regAdd, unsigned char data);
extern int spiWriteBytes(int spiFD, unsigned char data[], int len);
extern int spiSetMode(int spiFD, uint8_t mode);
extern int spiSetSpeed(int spiFD, uint32_t speed);
extern int spiSetBitsPerWord(int spiFD, uint8_t bpw);
extern void spiClose(int spiFD);

#ifdef __cplusplus
}
#endif /* __cplusplus */
#endif /* __BBBANDROIDHAL_H__ */
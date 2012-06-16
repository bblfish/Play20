package play.core.server.netty;

import java.io.*;

public class FakeKeyStore {
    
    private static final short[] DATA = new short[] {
            0xfe, 0xed, 0xfe, 0xed, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x00, 0x05, 0x6d, 0x79, 0x6b, 0x65, 0x79, 0x00, 0x00, 0x01, 0x37, 0x92, 0x1e, 0x7e, 0x32, 0x00,
            0x00, 0x05, 0x02, 0x30, 0x82, 0x04, 0xfe, 0x30, 0x0e, 0x06, 0x0a, 0x2b, 0x06, 0x01, 0x04, 0x01,
            0x2a, 0x02, 0x11, 0x01, 0x01, 0x05, 0x00, 0x04, 0x82, 0x04, 0xea, 0xc3, 0x3e, 0x58, 0xcf, 0x0b,
            0xcb, 0x72, 0x3b, 0xaa, 0xbc, 0x55, 0x9b, 0x78, 0x72, 0x8a, 0x1d, 0x34, 0x03, 0xde, 0xfd, 0xd6,
            0xe0, 0xf2, 0x80, 0xa5, 0xa9, 0xe9, 0x8f, 0x89, 0xe1, 0x41, 0xf8, 0x2a, 0x2e, 0xe5, 0x42, 0x94,
            0x3b, 0x95, 0x50, 0x7d, 0xa9, 0x0b, 0xb6, 0x54, 0x8d, 0xf9, 0x05, 0x08, 0x54, 0x8b, 0xbc, 0xb8,
            0x68, 0x5a, 0x62, 0xab, 0xed, 0xa3, 0xf6, 0x53, 0x80, 0x8a, 0x80, 0xab, 0x3e, 0x7a, 0x40, 0x7a,
            0x8d, 0x84, 0x6d, 0x07, 0xa4, 0x0b, 0xbb, 0xfd, 0x39, 0xf1, 0x4b, 0xbd, 0x90, 0x23, 0x07, 0xc8,
            0xac, 0x9e, 0x41, 0x08, 0x1f, 0x8e, 0x99, 0x0d, 0x9f, 0x2d, 0x31, 0x85, 0x87, 0xc2, 0xea, 0xd9,
            0x40, 0x99, 0x72, 0xa7, 0x1b, 0xb1, 0xb6, 0x7a, 0x53, 0xb5, 0x99, 0xee, 0x02, 0x12, 0x37, 0xe8,
            0xca, 0x8b, 0xe3, 0x51, 0x61, 0xa0, 0xd7, 0xd6, 0xa1, 0x87, 0xc4, 0xfd, 0xa0, 0x56, 0xc1, 0xc5,
            0x1c, 0xfc, 0x95, 0x09, 0xe4, 0x0d, 0x5e, 0x7b, 0x59, 0xcb, 0xe2, 0xbe, 0xbe, 0x69, 0xf2, 0x9c,
            0x7d, 0xb6, 0xe0, 0x2d, 0x86, 0x94, 0xbb, 0x89, 0x2a, 0xe0, 0x26, 0x6d, 0x5a, 0x78, 0x49, 0xa1,
            0x80, 0xea, 0x3a, 0xe7, 0x33, 0x48, 0x72, 0xfb, 0x85, 0xbb, 0xf3, 0x3c, 0x10, 0x96, 0x58, 0x02,
            0xa5, 0x11, 0xb8, 0xa1, 0x8b, 0x1e, 0x7b, 0x2d, 0x86, 0x21, 0x55, 0x7c, 0x1f, 0x37, 0xf3, 0x76,
            0xfc, 0xda, 0x47, 0x11, 0x29, 0xc6, 0x74, 0xc8, 0x85, 0x7f, 0x50, 0x3c, 0x70, 0xba, 0x57, 0xa0,
            0x80, 0x1c, 0x1f, 0x5f, 0x61, 0x5e, 0xbc, 0x5c, 0xd9, 0x37, 0xfc, 0xf8, 0x8f, 0x17, 0xfc, 0x6a,
            0x04, 0x72, 0x33, 0x38, 0xdc, 0x4b, 0x3d, 0x40, 0xf0, 0xe3, 0x12, 0x28, 0xa2, 0x2e, 0x9a, 0x44,
            0x48, 0xcb, 0x29, 0xe5, 0x4f, 0xd1, 0x61, 0xc1, 0x31, 0x0e, 0x1c, 0xfa, 0x5e, 0x0b, 0xbf, 0x90,
            0x55, 0x27, 0x4e, 0x1c, 0x5c, 0xa9, 0x9a, 0x9f, 0xc3, 0x1b, 0x47, 0xf5, 0x90, 0xd9, 0xa2, 0x57,
            0xac, 0x40, 0x8a, 0xa6, 0xbd, 0x0a, 0x43, 0x99, 0x2e, 0x8e, 0x23, 0x07, 0x39, 0xf9, 0x87, 0xb8,
            0xe6, 0x33, 0xee, 0x41, 0x6f, 0x58, 0x69, 0x4a, 0xf7, 0x39, 0x0e, 0x3d, 0xec, 0x88, 0x37, 0x47,
            0x79, 0x33, 0x46, 0x98, 0xdf, 0x3f, 0x53, 0x33, 0x55, 0x60, 0x02, 0xec, 0x35, 0xf4, 0x1f, 0x65,
            0xe5, 0x74, 0xb8, 0xdd, 0xd3, 0x02, 0x19, 0xa9, 0xfd, 0x8c, 0xf0, 0x24, 0xe9, 0x4d, 0xd8, 0xa6,
            0x6b, 0xd4, 0xaa, 0xe1, 0x2a, 0xb0, 0x68, 0x86, 0xea, 0x93, 0xbd, 0xfa, 0x90, 0x64, 0x2f, 0x39,
            0x4a, 0xf5, 0xa4, 0xb0, 0x4a, 0x46, 0x48, 0xca, 0xf8, 0xc2, 0xe5, 0x69, 0xda, 0x1e, 0x9b, 0x02,
            0xb7, 0x2b, 0xf0, 0x1d, 0xf7, 0x55, 0x9f, 0xe0, 0xc9, 0xd2, 0x5f, 0x08, 0x21, 0xfa, 0x85, 0x80,
            0xe8, 0x27, 0x0f, 0x65, 0x23, 0xaa, 0x9b, 0x49, 0xb1, 0x42, 0xe7, 0x2b, 0xe0, 0x46, 0x1d, 0x77,
            0x47, 0x47, 0x0b, 0xf2, 0x85, 0x17, 0xa9, 0x10, 0x7d, 0x41, 0x06, 0x34, 0xf1, 0x51, 0xb2, 0x45,
            0xfc, 0xaf, 0x8a, 0x57, 0xcc, 0xad, 0x64, 0x09, 0x36, 0xbb, 0x7a, 0x84, 0xb0, 0x84, 0x06, 0xa8,
            0x25, 0xe7, 0xef, 0x9c, 0x86, 0xc6, 0x8b, 0x07, 0x63, 0xdc, 0xc0, 0xed, 0x53, 0x3e, 0x27, 0xc0,
            0xe6, 0x8a, 0x94, 0x26, 0x95, 0x6c, 0x0d, 0xb0, 0x40, 0xb9, 0x57, 0xcd, 0x8d, 0xee, 0x05, 0xcd,
            0xc1, 0xe5, 0x79, 0x39, 0xd2, 0xb4, 0x3e, 0x2a, 0xc6, 0x78, 0x2b, 0x41, 0x32, 0xe2, 0xf0, 0xd6,
            0x79, 0x95, 0x61, 0xda, 0xf6, 0x07, 0xbc, 0xa0, 0x62, 0x2b, 0xbe, 0xa4, 0x48, 0x02, 0xcd, 0xef,
            0x12, 0xd5, 0x74, 0xa1, 0x07, 0xbc, 0xb3, 0xdc, 0xde, 0xa8, 0xfa, 0x7c, 0x3c, 0x8e, 0x89, 0x13,
            0x29, 0x5a, 0xd7, 0xa5, 0x92, 0x93, 0x1e, 0x4d, 0x31, 0x53, 0x30, 0xda, 0xb9, 0x3f, 0xa6, 0x78,
            0xfd, 0xb5, 0x66, 0xb4, 0x75, 0x8f, 0xf8, 0x1d, 0x0b, 0x1c, 0x90, 0x15, 0x8f, 0xdc, 0xc5, 0xb3,
            0xac, 0xba, 0x56, 0x1c, 0xac, 0xfd, 0xeb, 0xe2, 0x67, 0x30, 0xb5, 0xb9, 0x9c, 0xc6, 0x92, 0x66,
            0x2c, 0x09, 0x1d, 0xab, 0x80, 0x48, 0x1c, 0xac, 0x8e, 0xff, 0x0d, 0xc3, 0x2e, 0x1a, 0x14, 0x16,
            0x0c, 0x64, 0x5a, 0xb4, 0x4d, 0x27, 0x12, 0x08, 0xa5, 0x6f, 0xb9, 0xce, 0xe9, 0x28, 0xae, 0x22,
            0xbb, 0x4d, 0x19, 0xd9, 0xff, 0xc7, 0xa4, 0x87, 0x71, 0xaa, 0x88, 0x95, 0xd8, 0x75, 0x34, 0x8a,
            0x94, 0x94, 0xf6, 0x48, 0xf0, 0xaa, 0xd1, 0x33, 0x12, 0x0e, 0x07, 0xbb, 0x63, 0x04, 0x75, 0x18,
            0x1c, 0x54, 0x4f, 0x1e, 0x1e, 0x73, 0xee, 0xb4, 0x0e, 0x99, 0xda, 0x1b, 0x59, 0xda, 0xd2, 0x21,
            0x58, 0xce, 0xe1, 0xfa, 0x9f, 0x7b, 0x76, 0x54, 0x18, 0x69, 0x4b, 0xb5, 0x56, 0x73, 0x63, 0xe9,
            0x62, 0xdc, 0x03, 0xea, 0x48, 0x3f, 0x93, 0x8c, 0x20, 0xbf, 0xc9, 0x94, 0xf0, 0x83, 0x1a, 0x39,
            0x70, 0x09, 0x48, 0x9c, 0x0d, 0x7a, 0x0c, 0x88, 0x5d, 0x1d, 0x8c, 0x1e, 0x59, 0x9a, 0xa4, 0xd4,
            0x4b, 0x01, 0x72, 0xdc, 0x97, 0xe8, 0x41, 0x50, 0xad, 0x0c, 0x85, 0x28, 0x6b, 0x35, 0x07, 0xbb,
            0x3a, 0x89, 0x65, 0x5c, 0xfe, 0x70, 0x78, 0xee, 0x4b, 0xc5, 0x41, 0xf0, 0x50, 0x4b, 0xa1, 0x73,
            0x49, 0x92, 0xb2, 0x8e, 0x02, 0xd2, 0xc3, 0x25, 0x30, 0xde, 0x34, 0x17, 0x55, 0xdc, 0xe1, 0x7a,
            0xca, 0xda, 0xf0, 0x59, 0x34, 0x86, 0xfc, 0xdd, 0xe5, 0x40, 0x0f, 0x08, 0x77, 0xdc, 0x37, 0x18,
            0xcd, 0x3d, 0x2d, 0x23, 0x74, 0x5a, 0x1c, 0x82, 0x68, 0x4e, 0xbc, 0xa9, 0x46, 0x23, 0xe8, 0x40,
            0xf3, 0x89, 0xb2, 0x6b, 0x29, 0x3b, 0xdd, 0x6f, 0xd6, 0xc4, 0x6f, 0x42, 0xd8, 0x30, 0xaf, 0xc1,
            0x94, 0x45, 0xde, 0x8a, 0xad, 0x81, 0xc3, 0x43, 0x98, 0xdf, 0x06, 0x9a, 0x2d, 0x73, 0x87, 0xf5,
            0xc2, 0xf1, 0xdb, 0x9b, 0xbb, 0x75, 0x22, 0xa9, 0x36, 0x17, 0xce, 0x73, 0x4c, 0x4a, 0x4b, 0x59,
            0x6f, 0x07, 0x18, 0x3c, 0x88, 0x2e, 0x53, 0xd4, 0x74, 0x5f, 0xca, 0xfe, 0x2b, 0x20, 0x6d, 0x58,
            0xf0, 0x9b, 0x62, 0xfd, 0xeb, 0x13, 0xf1, 0xf4, 0xa1, 0x82, 0xf1, 0xe8, 0x39, 0x3e, 0x00, 0x9b,
            0x6a, 0x2a, 0x84, 0xe6, 0xf4, 0xf7, 0xb1, 0x47, 0xa2, 0x11, 0xe7, 0xb0, 0x98, 0xa0, 0x9d, 0xb5,
            0x28, 0xbd, 0x68, 0x9c, 0x0b, 0xbc, 0x60, 0x2c, 0xb4, 0x57, 0xe7, 0x4b, 0x73, 0x76, 0x65, 0x04,
            0x68, 0x6e, 0x2d, 0xed, 0x3c, 0x9d, 0x42, 0x75, 0xff, 0x51, 0xd0, 0x8e, 0x1c, 0x13, 0x29, 0xbb,
            0x60, 0xd2, 0x43, 0x4c, 0x8d, 0x6f, 0x35, 0x26, 0xfb, 0x92, 0x12, 0x51, 0x9a, 0x08, 0x56, 0x5d,
            0x26, 0xe9, 0x36, 0x1f, 0xa8, 0x58, 0x17, 0xeb, 0x96, 0xe0, 0x52, 0x83, 0xe4, 0x6d, 0xb6, 0xa4,
            0xae, 0xc2, 0x06, 0xd7, 0x7a, 0x04, 0x15, 0xf5, 0x89, 0x93, 0x15, 0xf4, 0x13, 0x47, 0xb7, 0x74,
            0x17, 0x34, 0x59, 0x45, 0xb7, 0x49, 0xe1, 0xca, 0x1a, 0xe5, 0x5a, 0x81, 0xc4, 0x70, 0x30, 0x64,
            0x09, 0xf2, 0xce, 0x0d, 0x0f, 0xb2, 0xa1, 0xd8, 0xba, 0x5d, 0xf8, 0x3e, 0x96, 0xad, 0x30, 0xbf,
            0x11, 0xbe, 0xef, 0xc0, 0xda, 0x8b, 0xcd, 0x53, 0xa2, 0xd5, 0x87, 0x00, 0xe0, 0xd7, 0xaf, 0xef,
            0xeb, 0x2f, 0x01, 0x14, 0x14, 0x0f, 0xed, 0xa8, 0xb3, 0x3c, 0xbd, 0xc4, 0xf0, 0x23, 0x79, 0xbe,
            0x81, 0xce, 0x38, 0x8c, 0x04, 0x95, 0x05, 0x61, 0x11, 0x1b, 0x7a, 0xe6, 0xf6, 0xb4, 0xdd, 0x4c,
            0xa3, 0x46, 0x96, 0x2d, 0x0a, 0x7d, 0x17, 0xeb, 0x4f, 0x53, 0x3a, 0x62, 0x59, 0x60, 0x27, 0x4b,
            0x2f, 0x6f, 0xdf, 0x25, 0xe7, 0xbc, 0x7e, 0xe7, 0x9f, 0x2d, 0x59, 0x23, 0xac, 0x5f, 0x06, 0x87,
            0xba, 0xa9, 0x2c, 0xfc, 0xd2, 0x0f, 0x4c, 0x88, 0x33, 0xe8, 0x5c, 0xe9, 0x3d, 0xa3, 0x96, 0x25,
            0xc1, 0x16, 0xe6, 0xf7, 0x86, 0x1e, 0x1d, 0xc1, 0x5a, 0x6d, 0x92, 0x16, 0xe7, 0xe5, 0x27, 0x7b,
            0xfc, 0xfe, 0xe9, 0x3d, 0x6e, 0xcf, 0x07, 0x02, 0x08, 0x1b, 0xed, 0xb0, 0xd7, 0xd2, 0x98, 0x12,
            0xd3, 0x08, 0x68, 0x9a, 0x46, 0xb6, 0xfe, 0x09, 0x59, 0x8b, 0x75, 0xb3, 0x22, 0x9b, 0xdc, 0xe9,
            0xf3, 0x97, 0x05, 0x0b, 0x2b, 0x97, 0x89, 0x72, 0x2a, 0x93, 0x04, 0x7d, 0x4c, 0x74, 0x98, 0xf5,
            0xf2, 0xab, 0xb4, 0xbd, 0xe7, 0x9e, 0x2e, 0xe9, 0x3f, 0xcd, 0x88, 0x5f, 0x96, 0x32, 0x13, 0xe2,
            0xc9, 0xf5, 0xe6, 0x4f, 0x65, 0x1f, 0xd0, 0x8b, 0x27, 0xb3, 0xb2, 0x75, 0x2a, 0x8d, 0xdb, 0xd0,
            0x06, 0xdd, 0xa9, 0x98, 0xc8, 0x79, 0x77, 0xc6, 0x6e, 0xa7, 0x5c, 0xc8, 0x6c, 0xe8, 0xfc, 0x5e,
            0xfe, 0x6d, 0xd5, 0x05, 0x9b, 0x3f, 0x92, 0xc9, 0x4e, 0xc2, 0x5d, 0x1e, 0x88, 0x04, 0xe7, 0xb0,
            0xee, 0xf5, 0x18, 0xf1, 0x71, 0xfd, 0x8f, 0xa1, 0x68, 0x52, 0x6d, 0x5c, 0xe8, 0x98, 0xe1, 0xae,
            0xaf, 0xed, 0x29, 0xd9, 0xb9, 0x02, 0x68, 0x16, 0xec, 0x60, 0xde, 0xad, 0xb4, 0x71, 0xcb, 0x7e,
            0x89, 0x83, 0xc8, 0x82, 0x74, 0x5a, 0x03, 0x27, 0xbd, 0x45, 0x1f, 0x5a, 0x70, 0x2d, 0xf1, 0x81,
            0x9d, 0x02, 0x48, 0x4b, 0xd1, 0x00, 0x00, 0x00, 0x01, 0x00, 0x05, 0x58, 0x2e, 0x35, 0x30, 0x39,
            0x00, 0x00, 0x03, 0x91, 0x30, 0x82, 0x03, 0x8d, 0x30, 0x82, 0x02, 0x75, 0xa0, 0x03, 0x02, 0x01,
            0x02, 0x02, 0x04, 0x2c, 0x61, 0xbc, 0x73, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7,
            0x0d, 0x01, 0x01, 0x0b, 0x05, 0x00, 0x30, 0x77, 0x31, 0x0b, 0x30, 0x09, 0x06, 0x03, 0x55, 0x04,
            0x06, 0x13, 0x02, 0x43, 0x59, 0x31, 0x13, 0x30, 0x11, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x0a,
            0x43, 0x79, 0x62, 0x65, 0x72, 0x73, 0x70, 0x61, 0x63, 0x65, 0x31, 0x14, 0x30, 0x12, 0x06, 0x03,
            0x55, 0x04, 0x07, 0x13, 0x0b, 0x4d, 0x6f, 0x6f, 0x6e, 0x20, 0x42, 0x61, 0x73, 0x65, 0x20, 0x31,
            0x31, 0x12, 0x30, 0x10, 0x06, 0x03, 0x55, 0x04, 0x0a, 0x13, 0x09, 0x4d, 0x61, 0x76, 0x65, 0x72,
            0x69, 0x63, 0x6b, 0x73, 0x31, 0x15, 0x30, 0x13, 0x06, 0x03, 0x55, 0x04, 0x0b, 0x13, 0x0c, 0x55,
            0x6e, 0x69, 0x74, 0x20, 0x54, 0x65, 0x73, 0x74, 0x69, 0x6e, 0x67, 0x31, 0x12, 0x30, 0x10, 0x06,
            0x03, 0x55, 0x04, 0x03, 0x13, 0x09, 0x6c, 0x6f, 0x63, 0x61, 0x6c, 0x68, 0x6f, 0x73, 0x74, 0x30,
            0x1e, 0x17, 0x0d, 0x31, 0x32, 0x30, 0x35, 0x32, 0x38, 0x30, 0x36, 0x32, 0x35, 0x30, 0x30, 0x5a,
            0x17, 0x0d, 0x33, 0x39, 0x31, 0x30, 0x31, 0x34, 0x30, 0x36, 0x32, 0x35, 0x30, 0x30, 0x5a, 0x30,
            0x77, 0x31, 0x0b, 0x30, 0x09, 0x06, 0x03, 0x55, 0x04, 0x06, 0x13, 0x02, 0x43, 0x59, 0x31, 0x13,
            0x30, 0x11, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x0a, 0x43, 0x79, 0x62, 0x65, 0x72, 0x73, 0x70,
            0x61, 0x63, 0x65, 0x31, 0x14, 0x30, 0x12, 0x06, 0x03, 0x55, 0x04, 0x07, 0x13, 0x0b, 0x4d, 0x6f,
            0x6f, 0x6e, 0x20, 0x42, 0x61, 0x73, 0x65, 0x20, 0x31, 0x31, 0x12, 0x30, 0x10, 0x06, 0x03, 0x55,
            0x04, 0x0a, 0x13, 0x09, 0x4d, 0x61, 0x76, 0x65, 0x72, 0x69, 0x63, 0x6b, 0x73, 0x31, 0x15, 0x30,
            0x13, 0x06, 0x03, 0x55, 0x04, 0x0b, 0x13, 0x0c, 0x55, 0x6e, 0x69, 0x74, 0x20, 0x54, 0x65, 0x73,
            0x74, 0x69, 0x6e, 0x67, 0x31, 0x12, 0x30, 0x10, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x09, 0x6c,
            0x6f, 0x63, 0x61, 0x6c, 0x68, 0x6f, 0x73, 0x74, 0x30, 0x82, 0x01, 0x22, 0x30, 0x0d, 0x06, 0x09,
            0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00, 0x03, 0x82, 0x01, 0x0f, 0x00,
            0x30, 0x82, 0x01, 0x0a, 0x02, 0x82, 0x01, 0x01, 0x00, 0xb9, 0x35, 0x73, 0xc2, 0xb4, 0x38, 0x04,
            0x57, 0xed, 0xd5, 0xee, 0xd7, 0xe4, 0x34, 0x64, 0xec, 0xc9, 0xf4, 0xef, 0x76, 0x39, 0x69, 0xc8,
            0x0c, 0xa0, 0x8b, 0x54, 0x05, 0x65, 0xe6, 0xb3, 0xda, 0x5b, 0x9d, 0x6c, 0x39, 0x8e, 0xda, 0x1f,
            0xb4, 0xf7, 0xc7, 0xe3, 0x75, 0x07, 0xee, 0xd9, 0x4b, 0xa7, 0xe0, 0xc9, 0xa6, 0xa5, 0xdc, 0x2b,
            0xf9, 0x2a, 0x9f, 0x81, 0xd9, 0x34, 0xa6, 0x0a, 0xc3, 0xa9, 0x4a, 0x47, 0xb0, 0x28, 0xaa, 0x48,
            0x79, 0x77, 0x39, 0xaa, 0xe4, 0x50, 0xf4, 0x4d, 0xf7, 0x7e, 0xf4, 0x6f, 0x0c, 0x98, 0x8f, 0x2c,
            0x0c, 0x05, 0x31, 0x35, 0x56, 0x75, 0x61, 0x57, 0x20, 0x70, 0x73, 0x70, 0x9f, 0xa2, 0x08, 0x7c,
            0x83, 0x04, 0x53, 0x52, 0x4c, 0x75, 0x10, 0x36, 0x80, 0xd1, 0x56, 0x5e, 0x72, 0xfe, 0xfb, 0xb0,
            0x86, 0xc9, 0x4d, 0x70, 0x80, 0x71, 0x69, 0x74, 0x65, 0x85, 0x33, 0x34, 0x32, 0x86, 0x0c, 0x16,
            0x94, 0x0a, 0x4c, 0xc0, 0x47, 0x45, 0xe4, 0x9d, 0x8e, 0xf9, 0x0c, 0x29, 0x22, 0x5a, 0x09, 0xd6,
            0xd1, 0xdc, 0x8a, 0xd7, 0xb9, 0x59, 0x2a, 0xb5, 0xed, 0x8c, 0xb4, 0x8a, 0x1a, 0xe9, 0x34, 0x32,
            0x99, 0xc0, 0x94, 0xf1, 0x29, 0xdf, 0xdd, 0x1b, 0x36, 0x44, 0x0d, 0x3b, 0xc4, 0xd3, 0x83, 0x8e,
            0x3b, 0xd9, 0xbc, 0xd2, 0x87, 0xf4, 0x5e, 0xad, 0x4c, 0x36, 0xfd, 0xd3, 0xbd, 0x3c, 0xfe, 0x24,
            0x0d, 0xf0, 0xbb, 0xfc, 0xbd, 0xb5, 0x74, 0x4e, 0xbe, 0x07, 0x15, 0x94, 0x9f, 0xf8, 0xf4, 0x6f,
            0xed, 0x7f, 0x81, 0x6e, 0xa0, 0x67, 0x3d, 0xd7, 0x04, 0x66, 0x1c, 0x66, 0x27, 0xf9, 0x31, 0xff,
            0x5d, 0x66, 0xad, 0xcc, 0xd3, 0xbb, 0x94, 0x5d, 0xce, 0xe0, 0x34, 0xf5, 0x9a, 0xc5, 0xa4, 0x26,
            0x3f, 0x0f, 0x4c, 0x4b, 0x77, 0x0c, 0x50, 0x3c, 0xb3, 0x02, 0x03, 0x01, 0x00, 0x01, 0xa3, 0x21,
            0x30, 0x1f, 0x30, 0x1d, 0x06, 0x03, 0x55, 0x1d, 0x0e, 0x04, 0x16, 0x04, 0x14, 0xab, 0x18, 0x7a,
            0x2f, 0x6f, 0xad, 0xef, 0xb4, 0x9a, 0x1d, 0x17, 0x7d, 0x89, 0xdf, 0x06, 0x49, 0xdc, 0x3e, 0x41,
            0x18, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x0b, 0x05, 0x00,
            0x03, 0x82, 0x01, 0x01, 0x00, 0x69, 0x8d, 0x84, 0xe5, 0x82, 0xde, 0x62, 0x09, 0x75, 0xc0, 0x31,
            0xab, 0xcf, 0xbd, 0x2f, 0x06, 0x55, 0xdb, 0xda, 0xef, 0xba, 0xcc, 0x46, 0x9f, 0x7c, 0x84, 0xe1,
            0x85, 0x76, 0x35, 0xa0, 0xf5, 0xc9, 0x20, 0x2d, 0x67, 0x19, 0x28, 0x8b, 0x38, 0x73, 0x5c, 0x12,
            0xa0, 0x72, 0x4f, 0x3d, 0x52, 0x99, 0xf6, 0xc5, 0x06, 0x6d, 0xef, 0xd6, 0xd9, 0x76, 0x82, 0x6e,
            0x78, 0xaf, 0x9f, 0x3c, 0x83, 0x83, 0xe5, 0xb1, 0x11, 0x33, 0x6c, 0xb2, 0x79, 0xdd, 0xbb, 0x71,
            0x44, 0x97, 0x72, 0xd6, 0x2c, 0x91, 0xfe, 0x46, 0xfb, 0x5c, 0x4d, 0xd7, 0xb6, 0x9f, 0x82, 0xf5,
            0xb7, 0xb3, 0x38, 0xbb, 0x30, 0x42, 0x30, 0x46, 0x8f, 0xde, 0xf0, 0x45, 0xcf, 0x42, 0xf6, 0x67,
            0x9c, 0xce, 0xd8, 0x01, 0x79, 0x41, 0x81, 0xc2, 0x13, 0xa3, 0x62, 0xb9, 0x7f, 0xb2, 0x65, 0x9b,
            0x58, 0x89, 0x45, 0x9b, 0x79, 0x28, 0x2d, 0x4f, 0x5c, 0xb4, 0x20, 0xf9, 0xec, 0x27, 0xcf, 0x71,
            0x4c, 0x08, 0x40, 0x70, 0x34, 0xf3, 0xbe, 0x1b, 0xc8, 0xab, 0x23, 0xe2, 0xa5, 0xa1, 0x38, 0xd2,
            0x50, 0xa5, 0xcb, 0x06, 0xe1, 0x4b, 0xee, 0x4a, 0x94, 0xce, 0x63, 0x8a, 0x6b, 0x15, 0x2c, 0x19,
            0x3c, 0x09, 0x1f, 0x25, 0x6b, 0x3d, 0x02, 0x73, 0x0d, 0x0f, 0xfe, 0xf7, 0x6a, 0xe6, 0x25, 0x29,
            0x9e, 0x74, 0xbb, 0xe9, 0x6e, 0x5d, 0x31, 0x9c, 0x04, 0x18, 0x82, 0xe3, 0x6c, 0x4d, 0x4f, 0x2d,
            0x78, 0x2b, 0x6c, 0xec, 0x57, 0xa1, 0x1b, 0xa6, 0xb1, 0x81, 0xd5, 0x01, 0x76, 0xba, 0x98, 0x63,
            0xc7, 0xb7, 0x1e, 0x4f, 0x14, 0x15, 0x64, 0x17, 0x45, 0x8b, 0xcb, 0x35, 0x11, 0xfb, 0x3e, 0x3e,
            0x65, 0x19, 0x10, 0x06, 0xa2, 0xbf, 0x3d, 0xa1, 0x5c, 0x33, 0x95, 0x91, 0x6b, 0xbf, 0xf8, 0x4d,
            0x97, 0xa3, 0x7f, 0x2c, 0x87, 0x9b, 0x3b, 0xcc, 0x32, 0x23, 0x0c, 0x47, 0x52, 0xc2, 0xfa, 0x13,
            0x6f, 0x74, 0x2d, 0x33, 0x26, 0x7e, 0xc4, 0xe5, 0x25
    };

    public static InputStream asInputStream() {
        byte[] data = new byte[DATA.length];
        for (int i = 0; i < data.length; i ++) {
            data[i] = (byte) DATA[i];
        }
        return new ByteArrayInputStream(data);
    }

    public static char[] getCertificatePassword() {
        return "secret".toCharArray();
    }

    public static char[] getKeyStorePassword() {
        return "secret".toCharArray();
    }

    private FakeKeyStore() {}
    
}
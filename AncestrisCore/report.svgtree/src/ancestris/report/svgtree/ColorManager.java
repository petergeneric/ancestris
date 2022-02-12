/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2022 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.report.svgtree;

import java.awt.Color;

/**
 * Color manager for Report Svg Tree.
 *
 * @author Zurga
 */
public class ColorManager {

    public Color colorCujus = new Color(0xff, 0xff, 0x33);
    public Color color_m1 = new Color(0xff, 0xdd, 0x00);
    public Color color_m2 = new Color(0xce, 0xaa, 0x31);
    public Color color_m3 = new Color(0xff, 0xdd, 0xdd);
    public Color color_m4 = new Color(0xef, 0xae, 0xc6);
    public Color color_m5 = new Color(0xff, 0x82, 0xb5);
    public Color color_m6 = new Color(0xd6, 0x5d, 0x5a);
    public Color color_m7 = new Color(0xe7, 0xdb, 0xe7);
    public Color color_m8 = new Color(0xad, 0xcf, 0xff);
    public Color color_m9 = new Color(0xad, 0xae, 0xef);
    public Color color_m10 = new Color(0x84, 0x82, 0xff);
    public Color color_m11 = new Color(0xde, 0x55, 0xff);
    public Color color_m12 = new Color(0xce, 0xb6, 0xbd);
    public Color color_m13 = new Color(0xff, 0xff, 0xff);
    public Color color_p1 = new Color(0xff, 0xff, 0xdd);
    public Color color_p2 = new Color(0xde, 0xff, 0xde);
    public Color color_p3 = new Color(0x82, 0xff, 0x82);
    public Color color_p4 = new Color(0x1a, 0xe1, 0x1a);
    public Color color_p5 = new Color(0xa9, 0xd0, 0xa9);
    public Color color_p6 = new Color(0xa9, 0xd0, 0xbf);
    public Color color_p7 = new Color(0xbb, 0xbb, 0xbb);
    public Color color_p8 = new Color(0xaa, 0x95, 0x95);
    public Color color_p9 = new Color(0x9e, 0xa3, 0xb2);
    public Color color_p10 = new Color(0xcd, 0xd3, 0xe9);
    public Color color_p11 = new Color(0xdf, 0xe2, 0xe2);
    public Color color_p12 = new Color(0xfa, 0xfa, 0xfa);
    public Color color_p13 = new Color(0xff, 0xff, 0xff);

    private Color[] BOX_COLORS = new Color[27];

    public Color[] getBoxColors() {
        BOX_COLORS = new Color[]{
            color_m13, // -13
            color_m12, // -12
            color_m11, // -11
            color_m10, // -10
            color_m9, // -9
            color_m8, // -8
            color_m7, // -7
            color_m6, // -6
            color_m5, // -5
            color_m4, // -4
            color_m3, // -3
            color_m2, // -2
            color_m1, // -1

            colorCujus, // 0

            color_p1, // 1
            color_p2, // 2
            color_p3, // 3
            color_p4, // 4
            color_p5, // 5
            color_p6, // 6
            color_p7, // 7
            color_p8, // 8
            color_p9, // 9
            color_p10, // 10
            color_p11, // 11
            color_p12, // 12
            color_p13 // 13
        };
        return BOX_COLORS;
    }

    public int getColorGenerations() {
        return (BOX_COLORS.length - 1) / 2;
    }
}

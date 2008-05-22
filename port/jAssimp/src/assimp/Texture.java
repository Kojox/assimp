/*
---------------------------------------------------------------------------
Open Asset Import Library (ASSIMP)
---------------------------------------------------------------------------

Copyright (c) 2006-2008, ASSIMP Development Team

All rights reserved.

Redistribution and use of this software in source and binary forms,
with or without modification, are permitted provided that the following
conditions are met:

* Redistributions of source code must retain the above
  copyright notice, this list of conditions and the
  following disclaimer.

* Redistributions in binary form must reproduce the above
  copyright notice, this list of conditions and the
  following disclaimer in the documentation and/or other
  materials provided with the distribution.

* Neither the name of the ASSIMP team, nor the names of its
  contributors may be used to endorse or promote products
  derived from this software without specific prior
  written permission of the ASSIMP Development Team.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
---------------------------------------------------------------------------
*/


package assimp;

import java.util.Vector;
import java.awt.*;

/**
 * Represents an embedded texture. Sometimes textures are not referenced
 * with a path, instead they are directly embedded into the model file.
 * Example file formats doing this include MDL3, MDL5 and MDL7 (3D GameStudio).
 * Embedded textures are converted to an array of color values (RGBA).
 * <p/>
 * Embedded textures in compressed file formats, such as JPEG or DDS
 * are NOT supported by jAssimp.
 *
 * @author Aramis (Alexander Gessler)
 * @version 1.0
 */
public class Texture {

    private int width = 0;
    private int height = 0;

    private Color[] data = null;

    /**
     * Retrieve the height of the texture image
     *
     * @return Height, in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Retrieve the width of the texture image
     *
     * @return Width, in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the color at a given position of the texture
     *
     * @param x X coordinate, zero based
     * @param y Y coordinate, zero based
     * @return Color at this position
     */
    public Color getPixel(int x, int y) {

        assert(x < width && y < height);

        // map the color data in memory if required ...
        if (null == data) {
            try {
                this.MapColorData();
            } catch (NativeError nativeError) {
                return Color.black;
            }
        }
        return data[y * width + x];
    }

    /**
     * Internal helper function to map the native texture data into
     * a <code>java.awt.Color</code> array
     */
    private void MapColorData() throws NativeError {
        final int iNumPixels = width * height;

        // first allocate the output array
        data = new Color[iNumPixels];

        // now allocate a temporary output array
        byte[] temp = new byte[(iNumPixels) << 2];

        // and copy the native color data to it
        if (0xffffffff == this._NativeMapColorData(temp)) {
           throw new NativeError("Unable to map aiTexture into Java-VM");
        }

        // now convert the temporary representation to a Color array
        // (data is given in BGRA order, we need RGBA)
        for (int i = 0, iBase = 0; i < iNumPixels; ++i, iBase += 4) {
            data[i] = new Color(temp[iBase + 2], temp[iBase + 1], temp[iBase], temp[iBase + 3]);
        }
        return;
    }

    /**
     * JNI bridge call. For internal use only
     * The method maps the contents of the native aiTexture object into memory
     * the native memory area will be deleted afterwards.
     *
     * @param temp Output array. Assumed to be width * height * 4 in size
     * @return 0xffffffff if an error occured
     */
    private native int _NativeMapColorData(byte[] temp);
}

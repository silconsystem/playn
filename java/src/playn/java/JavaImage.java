/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.ResourceCallback;

class JavaImage implements CanvasImage {

  BufferedImage img;
  private JavaCanvas canvas;
  private Exception exception;

  JavaImage(BufferedImage img) {
    this.img = img;
  }

  public JavaImage(Exception assetLoadException) {
    this.exception = assetLoadException;

    // the caller will be notified that this image failed to load, but we also create an error
    // image so that subsequent attempts to use this image won't result in numerous follow-on
    // errors when the caller attempts to call width/height/etc.
    img = createErrorImage(100, 100);
  }

  @Override
  public Canvas canvas() {
    if (canvas == null) {
      canvas = new JavaCanvas(img.createGraphics(), width(), height());
    }
    return canvas;
  }

  @Override
  public void replaceWith(Image img) {
    Asserts.checkArgument(img instanceof JavaImage);
    this.img = ((JavaImage) img).img;
  }

  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    if (img == null) {
      callback.error(exception != null ? exception : new RuntimeException("Error loading image"));
    } else {
      callback.done(this);
    }
  }

  @Override
  public int width() {
    return img.getWidth();
  }

  @Override
  public int height() {
    return img.getHeight();
  }

  @Override
  public boolean isReady() {
    return (img != null && exception == null);
  }

  private BufferedImage createErrorImage(int width, int height) {
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    try {
      g.setColor(Color.red);
      for (int yy = 0; yy <= height/15; yy++) {
        for (int xx = 0; xx <= width/45; xx++) {
          g.drawString("ERROR", xx*45, yy*15);
        }
      }
    } finally {
      g.dispose();
    }
    return img;
  }
}

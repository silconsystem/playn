/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.java;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import playn.core.TextFormat;

class JavaTextLayout implements playn.core.TextLayout {

  private float width, height;
  private TextFormat format;
  private List<TextLayout> layouts = new ArrayList<TextLayout>();
  private Color textColor, altColor;

  public JavaTextLayout (Frame frame, String text, TextFormat format) {
    this.format = format;

    // convert our colors to Java-land
    textColor = JavaCanvasState.convertColor(format.textColor);
    Integer altARGB = format.effect.getAltColor();
    if (altARGB != null) {
      altColor = JavaCanvasState.convertColor(altARGB);
    }

    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    text = text.replace("\r\n", "\n").replace('\r', '\n');

    // set up an attributed character iterator so that we can measure the text
    AttributedString astring = new AttributedString(text);
    if (format.font != null) {
      astring.addAttribute(TextAttribute.FONT, ((JavaFont)format.font).jfont);
    }
    FontRenderContext fctx;
    Graphics2D gfx = (Graphics2D)frame.getGraphics();
    try {
      fctx = gfx.getFontRenderContext();
    } finally {
      gfx.dispose();
      gfx = null;
    }

    if (format.shouldWrap() || text.indexOf('\n') != -1) {
      LineBreakMeasurer measurer = new LineBreakMeasurer(astring.getIterator(), fctx);
      char eol = '\n'; // TODO: platform line endings?
      int lastPos = text.length();
      while (measurer.getPosition() < lastPos) {
        int nextRet = text.indexOf(eol, measurer.getPosition()+1);
        if (nextRet == -1) {
          nextRet = lastPos;
        }
        layouts.add(measurer.nextLayout(format.wrapWidth, nextRet, false));
      }
    } else {
      layouts.add(new TextLayout(astring.getIterator(), fctx));
    }

    // compute our width and height
    float twidth = 0, theight = 0;
    for (TextLayout layout : layouts) {
      Rectangle2D bounds = layout.getBounds();
      twidth = Math.max(twidth, getWidth(bounds));
      if (layout != layouts.get(0)) {
        theight += layout.getLeading(); // leading only applied to lines after 0
      }
      theight += (layout.getAscent() + layout.getDescent());
    }
    width = format.effect.adjustWidth(twidth);
    height = format.effect.adjustHeight(theight);
  }

  @Override
  public float width() {
    return width;
  }

  @Override
  public float height() {
    return height;
  }

  @Override
  public int lineCount() {
    return layouts.size();
  }

  @Override
  public TextFormat format() {
    return format;
  }

  void paint(Graphics2D gfx, float x, float y) {
    Color ocolor = gfx.getColor();

    if (format.effect instanceof TextFormat.Effect.Shadow) {
      TextFormat.Effect.Shadow seffect = (TextFormat.Effect.Shadow)format.effect;
      // if the shadow is negative, we need to move the real text down/right to keep everything
      // within our bounds
      float tx, sx, ty, sy;
      if (seffect.shadowOffsetX > 0) {
        tx = 0;
        sx = seffect.shadowOffsetX;
      } else {
        tx = -seffect.shadowOffsetX;
        sx = 0;
      }
      if (seffect.shadowOffsetY > 0) {
        ty = 0;
        sy = seffect.shadowOffsetY;
      } else {
        ty = -seffect.shadowOffsetY;
        sy = 0;
      }
      gfx.setColor(altColor);
      paintOnce(gfx, x + sx, y + sy);
      gfx.setColor(textColor);
      paintOnce(gfx, x + tx, y + ty);

    } else if (format.effect instanceof TextFormat.Effect.Outline) {
      // you might think that we could render TextLayout.getOutline() but the results are hideous;
      // this expensive but functional approach is way better looking
      gfx.setColor(altColor);
      paintOnce(gfx, x+0, y+0);
      paintOnce(gfx, x+0, y+1);
      paintOnce(gfx, x+0, y+2);
      paintOnce(gfx, x+1, y+0);
      paintOnce(gfx, x+1, y+2);
      paintOnce(gfx, x+2, y+0);
      paintOnce(gfx, x+2, y+1);
      paintOnce(gfx, x+2, y+2);

      gfx.setColor(textColor);
      paintOnce(gfx, x+1, y+1);

    } else {
      gfx.setColor(textColor);
      paintOnce(gfx, x, y);
    }

    gfx.setColor(ocolor);
  }

  void paintOnce(Graphics2D gfx, float x, float y) {
    float yoff = 0;
    for (TextLayout layout : layouts) {
      Rectangle2D bounds = layout.getBounds();
      // some fonts starting rendering inset to the right, and others start rendering at a negative
      // inset, blowing outside their bounding box (naughty!); for the former, we trim off that
      // inset and for the latter we shift everything to the right to ensure that we don't paint
      // outside our reported bounding box (so that someone can create a single canvas of bounding
      // box size and render this text layout into it at (0,0) and nothing will get cut off)
      float rx = (float)-bounds.getX() + format.align.getX(getWidth(bounds), width);
      yoff += layout.getAscent();
      layout.draw(gfx, x + rx, y + yoff);
      if (layout != layouts.get(0)) {
        yoff += layout.getLeading(); // add interline spacing
      }
      yoff += layout.getDescent();
    }
  }

  float getWidth(Rectangle2D bounds) {
    // if our text includes a negative inset, that needs to be tacked onto the width
    return (float)(Math.max(-bounds.getX(), 0) + bounds.getWidth());
  }
}

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
package playn.android;

import playn.core.Log;

class AndroidLog implements Log {

  @Override
  public void debug(String msg) {
    android.util.Log.d("playn", msg);
  }

  @Override
  public void debug(String msg, Throwable e) {
    android.util.Log.d("playn", msg, e);
  }

  @Override
  public void info(String msg) {
    android.util.Log.i("playn", msg);
  }

  @Override
  public void info(String msg, Throwable e) {
    android.util.Log.i("playn", msg, e);
  }

  @Override
  public void warn(String msg) {
    android.util.Log.w("playn", msg);
  }

  @Override
  public void warn(String msg, Throwable e) {
    android.util.Log.w("playn", msg, e);
  }

  @Override
  public void error(String msg) {
    android.util.Log.e("playn", msg);
  }

  @Override
  public void error(String msg, Throwable e) {
    android.util.Log.e("playn", msg, e);
  }
}

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

import playn.core.Storage;
import android.app.Activity;
import android.content.SharedPreferences;

public class AndroidStorage implements Storage {
  private static final String PREFS_NAME = "playn";
  private SharedPreferences settings;

  public AndroidStorage(Activity activity) {
    settings = activity.getSharedPreferences(PREFS_NAME, 0);
  }

  @Override
  public void setItem(String key, String data) throws RuntimeException {
    settings.edit().putString(key, data).commit();
  }

  @Override
  public void removeItem(String key) {
    settings.edit().remove(key).commit();
  }

  @Override
  public String getItem(String key) {
    return settings.getString(key, null);
  }

  @Override
  public boolean isPersisted() {
    return true;
  }
}

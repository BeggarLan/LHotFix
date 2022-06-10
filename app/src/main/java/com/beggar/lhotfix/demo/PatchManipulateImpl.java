package com.beggar.lhotfix.demo;

import java.util.List;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beggar.hotfix.patch.Patch;
import com.beggar.hotfix.patch.PatchManipulate;

/**
 * author: lanweihua
 * created on: 2022/6/10 11:54 下午
 * description:
 */
public class PatchManipulateImpl implements PatchManipulate {

  @Nullable
  @Override
  public List<Patch> fetchPatchList(@NonNull Context context) {
    return null;
  }

  @Override
  public boolean verifyPatch(@NonNull Context context, @NonNull Patch patch) {
    return false;
  }

}

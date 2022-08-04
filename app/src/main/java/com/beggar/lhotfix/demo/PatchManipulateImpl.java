package com.beggar.lhotfix.demo;

import static com.beggar.hotfix.base.Constants.PATCHED_CLASS_INFO_PROVIDER_IMPL_FULL_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beggar.hotfix.patch.Patch;
import com.beggar.hotfix.patch.PatchManipulate;

/**
 * author: BeggarLan
 * created on: 2022/6/10 11:54 下午
 * description:
 */
public class PatchManipulateImpl implements PatchManipulate {

  @Nullable
  @Override
  public List<Patch> fetchPatchList(@NonNull Context context) {
    List<Patch> patches = new ArrayList<>();

    // TODO: 2022/6/13  网络获取补丁列表
    Patch patch = new Patch();
    patch.setName("hotfix-test");
    // 设置补丁文件的本地路径
    patch.setPatchLocalPath(
        Environment.getExternalStorageDirectory().getPath() + File.separator + "robust" +
            File.separator + "patch");
    patch.setPatchedClassInfoProviderClassFullName(PATCHED_CLASS_INFO_PROVIDER_IMPL_FULL_NAME);

    patches.add(patch);
    return patches;
  }

  @Override
  public boolean verifyPatch(@NonNull Context context, @NonNull Patch patch) {
    return false;
  }

}

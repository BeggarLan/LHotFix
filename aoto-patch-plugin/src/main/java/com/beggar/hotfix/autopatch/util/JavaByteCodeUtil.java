package com.beggar.hotfix.autopatch.util;

import static com.beggar.hotfix.autopatch.util.JavaByteCodeUtil.TypeDescriptor.BOOL_TYPE;
import static com.beggar.hotfix.autopatch.util.JavaByteCodeUtil.TypeDescriptor.BYTE_TYPE;
import static com.beggar.hotfix.autopatch.util.JavaByteCodeUtil.TypeDescriptor.CHAR_TYPE;
import static com.beggar.hotfix.autopatch.util.JavaByteCodeUtil.TypeDescriptor.DOUBLE_TYPE;
import static com.beggar.hotfix.autopatch.util.JavaByteCodeUtil.TypeDescriptor.FLOAT_TYPE;
import static com.beggar.hotfix.autopatch.util.JavaByteCodeUtil.TypeDescriptor.INT_TYPE;
import static com.beggar.hotfix.autopatch.util.JavaByteCodeUtil.TypeDescriptor.LONG_TYPE;
import static com.beggar.hotfix.autopatch.util.JavaByteCodeUtil.TypeDescriptor.SHORT_TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * author: BeggarLan
 * created on: 2022/7/19 12:29 下午
 * description: 字节码工具类
 */
public class JavaByteCodeUtil {

  // 数组类型
  public static final char ARRAY_TYPE = '[';

  // Object类型名结束符：(Ljava/lang/Integer;FLjava/lang/String;)V
  public static final char OBJECT_NAME_END_FLAG = ';';

  // 八大基本类型的表示
  @Retention(RetentionPolicy.SOURCE)
  public @interface TypeDescriptor {
    public static final char OBJECT_TYPE = 'L'; // Object

    public static final char BOOL_TYPE = 'Z'; // boolean
    public static final char CHAR_TYPE = 'C'; // char
    public static final char BYTE_TYPE = 'B'; // byte
    public static final char SHORT_TYPE = 'S'; // short
    public static final char INT_TYPE = 'I'; // int
    public static final char LONG_TYPE = 'J'; // long
    public static final char FLOAT_TYPE = 'F'; // float
    public static final char DOUBLE_TYPE = 'D'; // double
  }

  /**
   * 是否是基本类型
   */
  public static boolean isBasicType(char typeDescriptor) {
    return BOOL_TYPE == typeDescriptor
        || CHAR_TYPE == typeDescriptor
        || BYTE_TYPE == typeDescriptor
        || SHORT_TYPE == typeDescriptor
        || INT_TYPE == typeDescriptor
        || LONG_TYPE == typeDescriptor
        || FLOAT_TYPE == typeDescriptor
        || DOUBLE_TYPE == typeDescriptor;
  }

  /**
   * 八大基本类型：字节码描述符 --> 对应的全称
   */
  public static String getByBasicTypeDescriptor(@TypeDescriptor char basicTypeDescriptor) {
    switch (basicTypeDescriptor) {
      case BOOL_TYPE:
        return "boolean";
      case CHAR_TYPE:
        return ("char");
      case BYTE_TYPE:
        return ("byte");
      case SHORT_TYPE:
        return ("short");
      case INT_TYPE:
        return ("int");
      case LONG_TYPE:
        return ("long");
      case FLOAT_TYPE:
        return ("float");
      case DOUBLE_TYPE:
        return ("double");
    }
    return "";
  }

}

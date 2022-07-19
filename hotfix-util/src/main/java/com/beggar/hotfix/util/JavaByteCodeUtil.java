package com.beggar.hotfix.util;

import static com.beggar.hotfix.util.JavaByteCodeUtil.TypeDescriptor.BOOL_TYPE;
import static com.beggar.hotfix.util.JavaByteCodeUtil.TypeDescriptor.BYTE_TYPE;
import static com.beggar.hotfix.util.JavaByteCodeUtil.TypeDescriptor.CHAR_TYPE;
import static com.beggar.hotfix.util.JavaByteCodeUtil.TypeDescriptor.DOUBLE_TYPE;
import static com.beggar.hotfix.util.JavaByteCodeUtil.TypeDescriptor.FLOAT_TYPE;
import static com.beggar.hotfix.util.JavaByteCodeUtil.TypeDescriptor.INT_TYPE;
import static com.beggar.hotfix.util.JavaByteCodeUtil.TypeDescriptor.LONG_TYPE;
import static com.beggar.hotfix.util.JavaByteCodeUtil.TypeDescriptor.SHORT_TYPE;

/**
 * author: lanweihua
 * created on: 2022/7/19 12:29 下午
 * description: 字节码工具类
 */
public class JavaByteCodeUtil {

  // Object类型名结束符：(Ljava/lang/Integer;FLjava/lang/String;)V
  public static final char OBJECT_NAME_END_FLAG = ';';

  // 八大基本类型的表示
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

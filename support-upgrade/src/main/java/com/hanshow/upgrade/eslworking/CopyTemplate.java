package com.hanshow.upgrade.eslworking;

public class CopyTemplate
{
  public static void main(String[] args)
  {
    if (args.length <= 0)
    {
      System.err.println("args length less than 0");
      return;
    }
    new com.hanshow.support.upgrade.eslworking.CopyTemplate().exec(args[0]);
  }
}

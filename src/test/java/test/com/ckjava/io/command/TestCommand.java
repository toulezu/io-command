package test.com.ckjava.io.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCommand {

	public static void main(String[] args) {
		String str = "sdfdsf${java -jar test}";
		Pattern pattern = Pattern.compile("(\\$\\{.*\\})");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find() && matcher.groupCount() == 1) {
		   String matcherStr = matcher.group(1); // 获取匹配的数字，从1开始
		   System.out.println(matcherStr.replaceAll("\\$\\{", "").replaceAll("\\}", ""));
		}
	}
}

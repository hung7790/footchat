package comp437.footchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamConverter {

	public String isToString(InputStream in) throws IOException {
		InputStreamReader is = new InputStreamReader(in);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();

		while (read != null) {
			// System.out.println(read);
			sb.append(read);
			read = br.readLine();

		}

		return sb.toString();
	}
}

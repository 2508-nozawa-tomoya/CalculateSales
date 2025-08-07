package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//指定したパスに存在するすべてのファイルの情報をfiles[]に格納
		File[] files = new File(args[0]).listFiles();

		List<File> rcdFiles = new ArrayList<>();

		//売上ファイルかどうかの判定
		//filesの要素の数だけファイル名の取得と判定を繰り返す
		for(int i = 0; i < files.length; i++) {
			if(files[i].getName().matches("^[0-9]{8}.rcd$")) {
				//売上ファイルであればListに格納
				rcdFiles.add(files[i]);
			}
		}


		for(int i = 0; i < rcdFiles.size(); i++) {
			//売上ファイルの読み込み
			BufferedReader br = null;

			try {
				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				//一行ずつ読み込み、読み込んだ内容をListに保持
				//itemsArray<> ={支店コード, 売上金額}
				String line;
				List<String> itemsArray = new ArrayList<>();
				while((line = br.readLine()) != null) {
					itemsArray.add(line);
				}

				//long型へ変換
				long fileSale = Long.parseLong(itemsArray.get(1));
				//読み込んだ売上金額を加算
				Long saleAmount = branchSales.get(itemsArray.get(0)) + fileSale;

				//加算した売上金額をMapに追加
				branchSales.put(itemsArray.get(0), saleAmount);

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
			} finally {
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
					}
				}
			}
		}


		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				//lineに代入された文字列を分割
				String[] items = line.split(",");

				//支店コードと支店名を保持
				branchNames.put(items[0], items[1]);

				//支店コードと売上初期値を保持
				branchSales.put(items[0], 0L);


				System.out.println(line);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);

			//拡張forでMapからすべてのkey(支店コード)を取得し、そのkeyをもとに支店名、売上金額を取得する
			for(String key : branchNames.keySet()) {

				//BufferedWriter writeメソッドがLong型では適応されないため売上金額をString型へ変換
				String saleAmount = String.valueOf(branchSales.get(key));

				bw.write(key + ",");
				bw.write(branchNames.get(key) + ",");
				bw.write(saleAmount);
				bw.newLine();
			}
			bw.close();
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		}
		return true;
	}

}

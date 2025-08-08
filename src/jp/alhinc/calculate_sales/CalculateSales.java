package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	private static final String FILENAME_NOT_CONSECUTIVE = "売上ファイル名が連番になっていません";
	private static final String ARITHMETIC_OVERFLOW = "合計⾦額が10桁を超えました";
	private static final String BRANCHCODE_NOT_EXIST = "該当ファイル名(00000001.rcdなど)の支店コードが不正です";
	private static final String SALESFILE_INVALID_FORMAT = "該当ファイル名(00000001.rcdなど)のフォーマットが不正です";

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

		//売上ファイルのソート
		Collections.sort(rcdFiles);

		//売上ファイルが連番になっているかの確認
		for(int i = 0; i < rcdFiles.size() -1; i++) {

			//比較する二つのファイル名の先頭から8文字（数字部分）を切り出し、int型に変換
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			//二つのファイル名の数字の差が１であるかを確認
			if((latter - former) != 1) {
				//差が1ではない場合コンソールにエラーメッセージを表示
				System.out.println(FILENAME_NOT_CONSECUTIVE);
				return;
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
				//売上ファイルの中身が2行かどうか
				if(itemsArray.size() != 2 ) {
					//売上ファイルが2行でなかったらエラーメッセージ表示
					System.out.println(SALESFILE_INVALID_FORMAT);
					return;
				}

				//売上ファイルの支店コードが支店定義ファイルに存在するか確認
				if(!branchNames.containsKey(itemsArray.get(0))) {
					//支店コードが存在しない場合エラーメッセージ表示
					System.out.println(BRANCHCODE_NOT_EXIST);
					return;
				}

				//long型へ変換
				long fileSale = Long.parseLong(itemsArray.get(1));
				//読み込んだ売上金額を加算
				Long saleAmount = branchSales.get(itemsArray.get(0)) + fileSale;

				//売上金額の合計が10桁を超えていないか確認
				if(saleAmount >= 10000000000L) {
					//売上金額が11桁以上であればエラーメッセージ表示
					System.out.println(ARITHMETIC_OVERFLOW);
					return;
				}


				//加算した売上金額をMapに追加
				branchSales.put(itemsArray.get(0), saleAmount);

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
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

			//ファイルの存在を確認
			if(!file.exists()) {
				//支店定義ファイルが存在しない場合、コンソールにエラーメッセージを出力
				System.out.println(FILE_NOT_EXIST);
				//処理を終了
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				//lineに代入された文字列を分割
				String[] items = line.split(",");

				//支店定義ファイルのフォーマットを確認
				if((items.length != 2) || (!items[0].matches("^[0-9]{3}"))) {
					//支店定義ファイルのフォーマットが正しくない場合エラーメッセージをコンソールに表示
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

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
		BufferedWriter bw = null;
		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			//拡張forでMapからすべてのkey(支店コード)を取得し、そのkeyをもとに支店名、売上金額を取得する
			for(String key : branchNames.keySet()) {

				//BufferedWriter writeメソッドがLong型では適応されないため売上金額をString型へ変換
				String saleAmount = String.valueOf(branchSales.get(key));

				bw.write(key + ",");
				bw.write(branchNames.get(key) + ",");
				bw.write(saleAmount);
				bw.newLine();
			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}

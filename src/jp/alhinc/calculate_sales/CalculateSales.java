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

	//商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LIST = "commodity.lst";

	//商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	//支店コードの正規表現
	private static final String BRANCH_CODE_REGEX = "^[0-9]{3}";

	//商品コードの正規表現
	private static final String COMMODITY_CODE_REGEX = "^[A-Za-z0-9]{8}";

	private static final String branch = "支店";
	private static final String commodity = "商品";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "定義ファイルのフォーマットが不正です";
	private static final String FILENAME_NOT_CONSECUTIVE = "売上ファイル名が連番になっていません";
	private static final String ARITHMETIC_OVERFLOW = "合計⾦額が10桁を超えました";
	private static final String BRANCHCODE_NOT_EXIST = "の支店コードが不正です";
	private static final String COMMODITYCODE_NOT_EXIST = "の商品コードが不正です";
	private static final String SALESFILE_INVALID_FORMAT = "のフォーマットが不正です";

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

		//商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();

		//商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		//コマンドライン引数が渡されているか確認
		if(args.length != 1) {
			//コマンドライン引数が1つ設定されていない場合エラーメッセージ表示
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branch, BRANCH_CODE_REGEX, branchNames, branchSales)) {
			return;
		}

		//商品定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_COMMODITY_LIST, commodity, COMMODITY_CODE_REGEX, commodityNames, commoditySales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//指定したパスに存在するすべてのファイルの情報をfiles[]に格納
		File[] files = new File(args[0]).listFiles();

		List<File> rcdFiles = new ArrayList<>();

		//売上ファイルかどうかの判定
		//filesの要素の数だけファイル名の取得と判定を繰り返す
		for(int i = 0; i < files.length; i++) {
			if((files[i].isFile()) && (files[i].getName().matches("^[0-9]{8}.rcd$"))) {
				//対象がファイルであり、「数字8桁.rcd」であればListに格納
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
			String rcdFileName = rcdFiles.get(i).getName();

			try {
				File file = new File(args[0], rcdFileName);
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				//一行ずつ読み込み、読み込んだ内容をListに保持
				//itemsArray<> ={支店コード,商品コード, 売上金額}
				String line;
				List<String> itemsArray = new ArrayList<>();
				while((line = br.readLine()) != null) {
					itemsArray.add(line);
				}
				//売上ファイルの中身が3行かどうか
				if(itemsArray.size() != 3 ) {
					//売上ファイルが3行でなかったらエラーメッセージ表示
					System.out.println(rcdFileName + SALESFILE_INVALID_FORMAT);
					return;
				}

				//売上ファイルの支店コードが支店定義ファイルに存在するか確認
				if(!branchNames.containsKey(itemsArray.get(0))) {
					//支店コードが存在しない場合エラーメッセージ表示
					System.out.println(rcdFileName + BRANCHCODE_NOT_EXIST);
					return;
				}

				//売上ファイルの商品コードが商品定義ファイルに存在するか確認
				if(!commodityNames.containsKey(itemsArray.get(1))) {
					//商品コードが存在しない場合エラーメッセージ表示
					System.out.println(rcdFileName + COMMODITYCODE_NOT_EXIST);
					return;
				}

				//売上ファイルの売上金額が数字なのか確認
				if(!itemsArray.get(2).matches("^[0-9]*$")) {
					//売上金額が数字出なかった場合にはエラーメッセージ表示
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//long型へ変換
				long fileSale = Long.parseLong(itemsArray.get(2));

				//支店別に売上金額を集計
				//読み込んだ売上金額を該当する支店の売上金額に加算
				Long branchSaleAmount = branchSales.get(itemsArray.get(0)) + fileSale;

				//売上金額の合計が10桁を超えていないか確認
				if(branchSaleAmount >= 10000000000L) {
					//売上金額が11桁以上であればエラーメッセージ表示
					System.out.println(ARITHMETIC_OVERFLOW);
					return;
				}

				//加算した売上金額を支店ごとの売上を保持するMapに追加
				branchSales.put(itemsArray.get(0), branchSaleAmount);

				//商品ごとに売上金額を集計
				//読み込んだ売上金額を該当する商品の売上金額に加算
				Long commoditySaleAmount = commoditySales.get(itemsArray.get(1)) + fileSale;

				//売上金額の合計が10桁を超えていないか確認
				if(commoditySaleAmount >= 10000000000L) {
					//売上金額が11桁以上であればエラーメッセージ表示
					System.out.println(ARITHMETIC_OVERFLOW);
					return;
				}

				//加算した売上金額を商品ごとの売上を保持するMapに追加
				commoditySales.put(itemsArray.get(0), commoditySaleAmount);

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

		//商品別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル	・商品定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店or商品
	 * @param 正規表現
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, String str , String regex, Map<String, String> names, Map<String, Long> sales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);

			//ファイルの存在を確認
			if(!file.exists()) {
				//(支店or商品)定義ファイルが存在しない場合、コンソールにエラーメッセージを出力
				System.out.println(str + FILE_NOT_EXIST);
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

				//(支店or商品)定義ファイルのフォーマットを確認
				if((items.length != 2) || (!items[0].matches(regex))) {
					//(支店or商品)定義ファイルのフォーマットが正しくない場合エラーメッセージをコンソールに表示
					System.out.println(str + FILE_INVALID_FORMAT);
					return false;
				}

				//(支店or商品)コードと(支店or商品)名を保持
				names.put(items[0], items[1]);

				//(支店or商品)コードと売上初期値を保持
				sales.put(items[0], 0L);

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
	 * 支店別集計ファイル・商品別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> names, Map<String, Long> sales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;
		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			//拡張forでMapからすべてのkey(支店コードまたは商品コード)を取得し、そのkeyをもとに支店名または商品名と売上金額を取得する
			for(String key : names.keySet()) {

				//BufferedWriter writeメソッドがLong型では適応されないため売上金額をString型へ変換
				String saleAmount = String.valueOf(sales.get(key));

				bw.write(key + ",");
				bw.write(names.get(key) + ",");
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

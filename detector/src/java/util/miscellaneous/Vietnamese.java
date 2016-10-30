package util.miscellaneous;

/**
 * Created by thinhhv on 25/08/2014.
 */
public class Vietnamese {
	public static String toRoot(String s) {
		s = s.replaceAll("á|à|ả|ã|ạ|ă|ắ|ặ|ằ|ẳ|ẵ|â|ấ|ầ|ẩ|ẫ|ậ", "a");
		s = s.replaceAll("đ", "d");
		s = s.replaceAll("é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ", "e");
		s = s.replaceAll("í|ì|ỉ|ĩ|ị", "i");
		s = s.replaceAll("ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ", "o");
		s = s.replaceAll("ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự", "u");
		s = s.replaceAll("ý|ỳ|ỷ|ỹ|ỵ", "y");
		s = s.replaceAll("Á|À|Ả|Ã|Ạ|Ă|Ắ|Ặ|Ằ|Ẳ|Ẵ|Â|Ấ|Ầ|Ẩ|Ẫ|Ậ", "A");
		s = s.replaceAll("Đ", "D");
		s = s.replaceAll("É|È|Ẻ|Ẽ|Ẹ|Ê|Ế|Ề|Ể|Ễ|Ệ", "E");
		s = s.replaceAll("Í|Ì|Ỉ|Ĩ|Ị", "I");
		s = s.replaceAll("Ó|Ò|Ỏ|Õ|Ọ|Ô|Ố|Ồ|Ổ|Ỗ|Ộ|Ơ|Ớ|Ờ|Ở|Ỡ|Ợ", "O");
		s = s.replaceAll("Ú|Ù|Ủ|Ũ|Ụ|Ư|Ứ|Ừ|Ử|Ữ|Ự", "U");
		s = s.replaceAll("Ý|Ỳ|Ỷ|Ỹ|Ỵ", "Y");
		return s;
	}
}
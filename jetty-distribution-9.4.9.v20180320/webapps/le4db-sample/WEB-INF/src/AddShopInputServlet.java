import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AddShopInputServlet extends HttpServlet {

	private String _dbname = null;

	public void init() throws ServletException {
		// iniファイルから自分のデータベース情報を読み込む
		String iniFilePath = getServletConfig().getServletContext()
				.getRealPath("WEB-INF/le4db.ini");
		try {
			FileInputStream fis = new FileInputStream(iniFilePath);
			Properties prop = new Properties();
			prop.load(fis);
			_dbname = prop.getProperty("dbname");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(true);

		String status = (String)session.getAttribute("add_shop_status");

		String errorMessage = "";
		String addStr = "";
		if(status != null) {
			switch(status) {
			case "reject_empty":
				errorMessage = "店舗名と住所を入力してください";
				break;
			case "reject_duplicate":
				errorMessage = "すでに登録されている店舗です";
				break;
			case "reject_error":
				errorMessage = "エラーが発生しました";
				break;
			case "accept":
				addStr = "<table border=\"1\"><th>店舗名</th><th>住所</th>\n"
                                 + "<tr><td>" + request.getParameter("shopname") + "</td><td>" + request.getParameter("shopaddress")
                                 + "</td></tr></table>\n"
                                 + "を登録しました<br><br>";
            	break;
			default:

			}
			session.removeAttribute("add_shop_status");
		}

		

		out.println("<html>");
		out.println("<body>");
		
		out.println("<h3>店舗登録</h3>");

		out.println("<h4><font color=\"red\">" + errorMessage + "</font></h4>");

		out.println(addStr);

		out.println("<form action=\"add_shop\" method=\"GET\">");
		out.println("店舗名: ");
		out.println("<input type=\"text\" name=\"shopname\"/>");
		out.println("<br>");
		out.println("住所: ");
		out.println("<input type=\"text\" name=\"shopaddress\"/>");
		out.println("<br>");
		out.println("<input type=\"submit\" value=\"追加\"/>");
		out.println("</form>");


		out.println("<br/>");
		out.println("<a href=\"shoplist_sv\">前のページに戻る</a>");

		out.println("</body>");
		out.println("</html>");
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}

}

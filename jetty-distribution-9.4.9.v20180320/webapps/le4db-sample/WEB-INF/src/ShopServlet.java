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
public class ShopServlet extends HttpServlet {

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
		
		String shopName = (String)session.getAttribute("shopname");
		String shopAddress = (String)session.getAttribute("shopaddress");
                if(shopName == null) {
			shopName = request.getParameter("shopname");
			shopAddress = request.getParameter("shopaddress");
			session.setAttribute("shopname", shopName);
			session.setAttribute("shopaddress", shopAddress);
		}

		String searchMail = request.getParameter("search_mail");
		if(searchMail == null || searchMail.equals("")) {
			searchMail = (String)session.getAttribute("search_mail");
		} else {
			session.setAttribute("search_mail", searchMail);
		}

		String filter = request.getParameter("filter");
		if(filter == null) {
			filter = (String)session.getAttribute("filter");
		} else {
			session.setAttribute("filter", filter);
		}

		Object status = session.getAttribute("return_status");		

		String usedAddressStr = "";
		String searchStr = "";
		if(searchMail != null && !searchMail.equals("")) {
			usedAddressStr = "検索したメールアドレス: " + searchMail + "<br>";
			searchStr = " and mail = '" + searchMail + "'";
		}
		
		String selectNo = "";
		String selectRentalOnly = "";
		switch(filter) {
		case "no":
			filter = "";
			selectNo = "selected";
			selectRentalOnly = "";
			break;
		case "rentalonly":
			filter = " and finished = 'no'";
			selectRentalOnly = "selected";
			break;
		default:
			filter = "";
		}

		String returnStr = "";
		if(status != null) {
			returnStr = "<h3><font color=\"red\">返却しました</font></h3><br>";
			session.removeAttribute("return_status");
		}

		out.println("<html>");
		out.println("<body>");
		out.println("<h3>" + shopName);
		out.println("<br><br>");
		out.println("貸出状況</h3>");
		out.println(returnStr);
		out.println("<a href=\"add_rental_input\"> 追加する</a>");
		out.println("<br>");
		out.println(usedAddressStr);
		out.println("<form action=\"shop\" method=\"GET\">");
                out.println("メールアドレスで検索: ");
                out.println("<input type=\"text\" name=\"search_mail\"/>");
		out.println("<input type=\"submit\" value=\"検索\"/>");
                out.println("<br>");
		out.println("<form action=\"shop\" method=\"GET\">");
		out.println("フィルター： ");
		out.println("<br>");
		out.println("<select name =\"filter\">");
		out.println("<option value=\"no\" " + selectNo + ">なし</option>");
		out.println("<option value=\"rentalonly\" " + selectRentalOnly + ">貸出中のみ</option>");
		out.println("</select>");
                out.println("<input type=\"submit\" value=\"適用\"/>");
		out.println("</form>");

		out.println("SELECT * FROM rent NATURAL INNER JOIN store WHERE mid = (SELECT mid FROM put WHERE shopname = '"
			                                 + shopName +  "' and shopaddress = '" + shopAddress + "'" + filter + ")" 
                                                         + searchStr + "ORDER BY return_date DESC");

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			out.println("<table border=\"1\">");
			out.println("<tr><th>メールアドレス</th><th>mid</th><th>タイトル</th><th>貸出日</th><th>返却日</th><th>状態</th><th>返却</th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM rent NATURAL INNER JOIN store WHERE mid = (SELECT mid FROM put WHERE shopname = '"
			                                 + shopName +  "' and shopaddress = '" + shopAddress + "'" + filter + ")" 
                                                         + searchStr + "ORDER BY return_date DESC");
			while (rs.next()) {
				String mailAddress = rs.getString("mail");
				String mid = rs.getString("mid");
                                String title = rs.getString("title");
				String rentalDate = rs.getString("rental_date");
				String returnDate = rs.getString("return_date");
				String state = rs.getString("finished").equals("yes") ? "返却済み" : "貸出中";

				out.println("<tr>");
				out.println("<td>" + mailAddress + "</td>");
				out.println("<td>" + mid + "</td>");
                                out.println("<td>" + title + "</td>");
				out.println("<td>" + rentalDate + "</td>");
				out.println("<td>" + returnDate + "</td>");
				out.println("<td>" + state + "</td>");
				if(state.equals("返却済み")) {
					out.println("<td></td>");
				} else {
					out.println("<td><a href=\"return?mail=" + mailAddress + "&mid=" + mid + "\">返却する</a></td>");
				}
				out.println("</tr>");
			}
			rs.close();
			
			out.println("</table>");
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		out.println("<br>");
		out.println("<a href=\"clerk\">前のページに戻る<a>");

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
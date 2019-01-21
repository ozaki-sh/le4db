import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
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
public class ShopListUSServlet extends HttpServlet {

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
		
		session.removeAttribute("shopname");
		session.removeAttribute("shopaddress");
		
		String searchShopName = request.getParameter("search_shopname");
		String searchShopAddress = request.getParameter("search_shopaddress");

		String usedNameStr = "";
		String usedAddressStr = "";
		String searchNameStr = "";
		String searchAddressStr = "";
		if(searchShopName != null && !searchShopName.equals("")) {
			session.setAttribute("search_shopname", searchShopName);
		} else {
			searchShopName = session.getAttribute("search_shopname");
		}		
		if(searchShopName != null && !searchShopName.equals("")) {
			searchNameStr = " and shopname LIKE '%" + searchShopName + "%'";
		} else {
			searchShopName = "";
		}
		
		if(searchShopAddress != null && !searchShopAddress.equals("")) {
			session.setAttribute("search_address", searchAddress);
		} else {
			searchAddress = session.getAttribute("search_address");
		}
		if(searchShopAddress != null && !searchShopAddress.equals("")) {
			searchAddressStr = " and shopaddress LIKE '%" + searchShopAddress + "%'";
		} else {
			searchShopName = "";
		}
		
		String order = request.getParameter("order");
		if(order == null) {
			order = (String)session.getAttribute("order");
		} else {
			session.setAttribute("order", order);
		}
		
		String selectAlphabet = "";
		String selectNum = "";
		switch(order) {
		case "alphabet":
			order = "shopname";
			selectAlphabet = "selected";
			break;
		case "total_num":
			order = "total_media";
			selectNum = "selected";
			break;
		default:
			order = "alphabet";
		}

		out.println("<html>");
		out.println("<body>");
		out.println("<h3>店舗検索結果</h3>");
		out.println("検索した店舗名： " + searchShopName);
		out.println("<br>");
		out.println("検索した住所： " + searchAddress);
		out.println("<br><br>");
		out.println("ソート： ");
		out.println("<select name =\"order\">");
		out.println("<option value=\"alphabet\" " + selectAlphabet + ">五十音順</option>");
		out.println("<option value=\"total_num\" " + selectNum + ">総所持数順</option>");
		out.println("</select>");
		out.println("<input type=\"submit\" value=\"適用\"/>");
		out.println(usedNameStr);
		out.println(usedAddressStr);
		
		if(usedNameStr.length() + usedAddressStr.length() != 0) {
			out.println("<br>");
		}


		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			out.println("<table border=\"1\">");
			out.println("<tr><th>店舗名</th><th>住所</th><th>総所持数</th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM shop WHERE 1 = 1"
                                                         + searchNameStr + searchAddressStr + " ORDER BY " + order + " DESC");
			while (rs.next()) {
				String shopName = rs.getString("shopname");
				String shopAddress = rs.getString("shopaddress");
				int totalMedia = rs.getInt("total_media");

				out.println("<tr>");
				out.println("<td><a href=\"medialist_in_shop?shopname=" + URLEncoder.encode(shopName, "UTF-8")
                                            + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8") + "\">" + shopName
                                            + "&formerURL=shoplist_us</a></td>");
				out.println("<td>" + shopAddress + "</td>");
				out.println("<td>" + totalMedia + "</td>");
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
		out.println("<a href=\"user\">前のページに戻る</a>");

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
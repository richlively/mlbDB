package view;

public class TeamView extends BaseView {

    public TeamView() {
        title = "Team";
    }
    
    @Override
    public void buildSearchForm() {
        body.append("<form action=\"");
        body.append(title.toLowerCase());
        body.append(".ssp\" method=\"get\">\r\n");
        body.append("Enter team name: <input type=\"text\" size=\"20\" name=\"name\"><br/><input type=\"checkbox\" name=\"exact\"> Exact Match?<br/>\r\n");
        body.append("<input type=\"hidden\" name=\"action\" value=\"search\">\r\n");
        body.append("<input type=\"submit\" value=\"Submit\">\r\n");
        body.append("</form>\r\n"); 
    }

}

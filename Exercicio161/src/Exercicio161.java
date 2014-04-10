
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ANTONIOFA
 */

interface Bolsa {
    boolean iniciar();
    boolean actualizar();
    boolean novo(String login, String clave, float capital);
    boolean identificar(String login, String clave);
}
    
interface Inversor {
    boolean comprar(int id, int cantidade);
    boolean vender(int id, int cantidade);
    float valorar();
}

interface Resumible {
    String resumir();
}



class BolsaEnBD implements Bolsa {
    private String login;
    private String clave;
    private float capital;
    public  static Connection con;

    
    public @Override
    boolean iniciar(){
        boolean correcto = false;
        try {
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3307/bolsa","root","qwerty");
            correcto = true;
        }catch(SQLException e){
            e.printStackTrace();
        }
        return correcto; 
    }
    
    public @Override
    boolean novo(String login, String clave, float capital){
        PreparedStatement ps = null;
        boolean correcto = false;
        try {
            ps = (PreparedStatement) con.prepareStatement("SELECT * FROM usuario WHERE login = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("O usuario xa existe");
            }else{
            ps = (PreparedStatement) con.prepareStatement("INSERT INTO usuario VALUES(?,?,?)");
            ps.setString(1, login);
            ps.setString(2, clave);
            ps.setFloat(3, capital);
            ps.executeUpdate();
            correcto = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(BolsaEnBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return correcto;
    }


    public boolean actualizar() {
        PreparedStatement ps = null;
        boolean actualizar = false;
        try {
            ps = (PreparedStatement) con.prepareStatement("SELECT * FROM acciones");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                actualizar = true;
                ps = (PreparedStatement) con.prepareStatement("UPDATE acciones SET valor = ? WHERE id = ?");
                ps.setDouble(1, 1 + (Math.random()*10/10) - (Math.random()*10/100)) ;
                ps.setInt(2, rs.getInt("id"));
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(BolsaEnBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return actualizar;
    }


    public boolean identificar(String login, String clave) {
        PreparedStatement ps = null;
        boolean correcto = false;
        try {
            ps = (PreparedStatement) con.prepareStatement("SELECT * FROM usuario WHERE login = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Benvido "+rs.getString("login"));
                correcto = true;
            }else{
                System.out.println("Datos Incorrectos");
            }
        } catch (SQLException ex) {
            Logger.getLogger(BolsaEnBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return correcto;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
    
    
}




class InversorPrivado implements Inversor, Resumible{
    
    private String login;
    
    public String resumir(){
        PreparedStatement ps = null;
        int usuarios = 0, accions = 0;
        try {
            ps = (PreparedStatement) BolsaEnBD.con.prepareStatement("SELECT COUNT(login) AS usuarios FROM usuario");
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                usuarios = rs.getInt("usuarios");
            }
            rs.close();
            
            ps = (PreparedStatement) BolsaEnBD.con.prepareStatement("SELECT COUNT(id) AS accions FROM acciones");
            ResultSet rs2 = ps.executeQuery();
            if(rs2.next()){
                accions = rs2.getInt("accions");
            }
            rs2.close();
        } catch (SQLException ex) {
            Logger.getLogger(BolsaEnBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Esta e a información da bolsa: \n Número de usuarios: "+usuarios+"\n Número de accións: "+accions;
    }
    
    public @Override
    boolean comprar(int id, int cantidade){
        int cantidadeac = 0;
        double valor = 0;
        boolean comprar = false;
        try {
            PreparedStatement comprobar  = (PreparedStatement) BolsaEnBD.con.prepareStatement("SELECT * FROM cantidad WHERE idaccion = ?");
            comprobar.setInt(1, id);
            ResultSet rs = comprobar.executeQuery();
            if(rs.next()){
                cantidadeac = rs.getInt("cantidad");
                PreparedStatement ps = (PreparedStatement) BolsaEnBD.con.prepareStatement("UPDATE cantidad SET cantidad = ? WHERE login = ? AND idaccion = ?");
                ps.setString(2, login);
                ps.setInt(3, id);
                ps.setInt(1, cantidade+rs.getInt("cantidad"));
                ps.executeUpdate();
                System.out.println("Datos modificados correctamente");
                
                PreparedStatement ps2 = (PreparedStatement) BolsaEnBD.con.prepareStatement("SELECT * FROM acciones WHERE id = ?");
                ps2.setInt(1, rs.getInt("idaccion"));
                ResultSet rs2 = ps2.executeQuery();
                while(rs2.next()){
                    valor = rs2.getDouble("valor");
                }
                
            }else{
                PreparedStatement ps = (PreparedStatement) BolsaEnBD.con.prepareStatement("INSERT INTO cantidad VALUES(?,?,?)");
                ps.setString(1, login);
                ps.setInt(2, id);
                ps.setInt(3, cantidade);
                ps.executeUpdate();
                System.out.println("Datos ingresados correctamente");
            }
            
            
            
            PreparedStatement ps3 = (PreparedStatement) BolsaEnBD.con.prepareStatement("SELECT * FROM usuario WHERE login = ?");
            ps3.setString(1, login);
            ResultSet rs3 = ps3.executeQuery();
                if(rs3.next()){
                    ps3 = (PreparedStatement) BolsaEnBD.con.prepareStatement("UPDATE usuario SET capital = ? WHERE login = ?");
                    ps3.setDouble(1, (rs3.getInt("capital"))-(cantidadeac*valor));
                    ps3.setString(2, rs3.getString("login"));
                    ps3.executeUpdate();
                }
            
        } catch (SQLException ex) {
            Logger.getLogger(InversorPrivado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return comprar;
    }

    @Override
    public boolean vender(int id, int cantidade) {
        boolean vender = false;
        int cantidadeac = 0;
        double valor = 0;
        try {
            PreparedStatement comprobar  = (PreparedStatement) BolsaEnBD.con.prepareStatement("SELECT * FROM cantidad WHERE idaccion = ?");
            comprobar.setInt(1, id);
            ResultSet rs = comprobar.executeQuery();
                if(rs.next()){
                        PreparedStatement ps = (PreparedStatement) BolsaEnBD.con.prepareStatement("UPDATE cantidad SET cantidad = ? WHERE login = ? AND idaccion = ?");
                        ps.setString(2, login);
                        ps.setInt(3, id);
                        ps.setInt(1, (rs.getInt("cantidad")-(cantidade)));
                        ps.executeUpdate();
                        System.out.println("Datos modificados correctamente");
                        
                        PreparedStatement ps2 = (PreparedStatement) BolsaEnBD.con.prepareStatement("SELECT * FROM acciones WHERE id = ?");
                        ps2.setInt(1, rs.getInt("idaccion"));
                        ResultSet rs2 = ps2.executeQuery();
                        while(rs2.next()){
                            valor = rs2.getInt("valor");
                        }
                }else{
                    System.out.println("No tienes acciones para vender");
                }
                
                 PreparedStatement ps3 = (PreparedStatement) BolsaEnBD.con.prepareStatement("SELECT * FROM usuario WHERE login = ?");
                ps3.setString(1, login);
                ResultSet rs3 = ps3.executeQuery();
                    if(rs3.next()){
                        ps3 = (PreparedStatement) BolsaEnBD.con.prepareStatement("UPDATE usuario SET capital = ? WHERE login = ?");
                        ps3.setDouble(1, (rs3.getInt("capital"))+(cantidadeac*valor));
                        ps3.setString(2, rs3.getString("login"));
                        ps3.executeUpdate();
                    }
        } catch (SQLException ex) {
            Logger.getLogger(InversorPrivado.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vender;
    }


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public float valorar() {
        float resultado = 0;
        try {
            PreparedStatement ps = (PreparedStatement)BolsaEnBD.con.prepareStatement(
                    "SELECT SUM(cantidad*valor) AS total" +
                    " FROM acciones ac INNER JOIN cantidad c" +
                    " ON ac.idaccion=c.id WHERE login=?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado = rs.getFloat("total");
            }   
        } catch(SQLException e){
            System.out.println("O usuario " + login + " comprou valores");
        }
        return resultado;
    }
    
    
}

public class Exercicio161 {

    /**
     * @param args the command line arguments
     */
    

    
    
    public static void main(String[] args) {
        BolsaEnBD bolsa1 = new BolsaEnBD();
        if(bolsa1.iniciar()){
            bolsa1.actualizar();
            if(bolsa1.identificar("Antonio", "1234")){
                InversorPrivado i1 = new InversorPrivado();
                i1.setLogin("Antonio");
                //i1.comprar(1, 1000);
                //i1.comprar(2, 1000);
                //i1.vender(1, 1000);
                i1.resumir();
                i1.valorar();
            }
        }
    }
}

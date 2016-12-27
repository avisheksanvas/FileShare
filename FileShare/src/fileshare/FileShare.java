/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileshare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avishek
 */
public class FileShare {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try 
        {
            ServerSocket s = new ServerSocket(1234) ;
            while( true )
            {
                new Thread( new client(s.accept() )).start();
            }
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(FileShare.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

class client implements Runnable
{
    private Socket s ;
    private DataInputStream din ;
    private DataOutputStream dout ;
    public client( Socket ss) 
    {
        try 
        {
            s = ss ;
            din = new DataInputStream( s.getInputStream() ) ;
            dout = new DataOutputStream( s.getOutputStream() );
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void run() 
    {
        RandomAccessFile rw = null ;
        long current_file_pointer = 0 ;
        boolean loop_break = false ;
        while( true )
        {
            byte[] initialize = new byte[1] ;
            try 
            {
                din.read( initialize , 0 , initialize.length ) ;
                if( initialize[0] == 2 )
                {
                    //System.out.println(new String(Read() ));
                    byte[] cmd_buff = new byte[3] ;
                    din.read(cmd_buff , 0 , cmd_buff.length ) ;
                    byte[] recv_data = Read() ;
                
                    switch( Integer.parseInt(new String(cmd_buff)) )
                    {
                        case 124:
                            rw = new RandomAccessFile("/Users/Avishek/Downloads" + new String(recv_data),"rw") ;
                            dout.write(Create("125".getBytes("UTF8")  , String.valueOf(current_file_pointer).getBytes("UTF8")));
                            dout.flush() ;
                            break; 
                        case 126:
                            rw.seek(current_file_pointer);
                            rw.write(recv_data);
                            
                            current_file_pointer = rw.getFilePointer();
                            System.out.println("Downloaded :"+ ((float)current_file_pointer/rw.length())*100+"%");
                                    
                            dout.write(Create("125".getBytes("UTF8")  , String.valueOf(current_file_pointer).getBytes("UTF8")));
                            dout.flush() ;
                            break;
                        case 127:
                            if( new String(recv_data) == "Close" )
                            {
                                loop_break = true ; 
                            }
                            break;
                               
                    }
                    
                    
                } 
                if( loop_break == true )
                {
                   s.close();
                }
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(client.class.getName()).log(Level.SEVERE, null, ex);
            }
            
                
    }
    
    }
    private byte[] Read()
    {
        byte[] data_buff = null ;
        try 
        {
            int b = 0 ;
            String length_buff = "" ;
            while( ( b = din.read() )  != 4 )
            {
                length_buff += (char)b ; 
            }
            int data_length = Integer.parseInt(length_buff) ;
            data_buff = new byte[Integer.parseInt(length_buff)] ;
            int byte_read = 0 ;
            int byte_offset = 0 ; 
            while( byte_offset < data_length )
            {
                byte_read = din.read(data_buff , byte_offset , data_length- byte_offset) ;
                byte_offset += byte_read ; 
            }
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data_buff ;
    }
    
    private byte[] Create( byte[] cmd , byte[] data )
    {
        byte[] packet = null ;
        try 
        {
            byte [] initialize = new byte[1] ;
            initialize[0] = 2 ;
            byte [] separator = new byte[1] ;
            separator[0] = 4 ;
            byte [] data_length = String.valueOf(data.length).getBytes("UTF8") ;
            packet = new byte[initialize.length + cmd.length + separator.length + data_length.length + data.length ] ;
            System.arraycopy( initialize , 0, packet , 0 , initialize.length );
            System.arraycopy( cmd , 0, packet , initialize.length , cmd.length );
            
            System.arraycopy( data_length , 0, packet , initialize.length + cmd.length, data_length.length);
            
            System.arraycopy( separator , 0, packet , initialize.length + cmd.length + data_length.length , separator.length );
            System.arraycopy( data , 0, packet , initialize.length + cmd.length + separator.length + data_length.length , data.length );
            
            
            
        } 
        catch (UnsupportedEncodingException ex) 
        {
            Logger.getLogger(FileShare.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packet ;
        
    }
}

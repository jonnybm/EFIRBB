import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.faces.bean.ManagedBean;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;

import com.sun.prism.paint.Color;
import com.sun.xml.internal.ws.util.StringUtils;
 

//


@ManagedBean
public class EFIRBB {
	
	private static GetSetBB getSetBB = new GetSetBB();
	private static EficienciaFinanceiraIRBB EficienciaFinanceiraIRBB = new EficienciaFinanceiraIRBB();
	private static String banco = "IRBB";  // BB ou CEF
	

  	static String caminho = "";
  	static String lixo = "";
  	static String excelBB = "";
  	static String ComboMes = "";
  	static String ComboAno = "";
  	static String ComboAdd = "";
	static int tamanhoLinhas = 0;
	static boolean IR = false;
	static boolean Capital = false;
	
  	static ArrayList<String> arrayPublicoCJExisteUnica = new ArrayList<String> ();
  	
  	
  	static File fo = new File(excelBB);

	//public static void main(String[] args) throws IOException {	
	public static void init() throws IOException, InterruptedException {
		fo = new File(excelBB);
		visualizarArquivos();
		
	}

	
	  public static void visualizarArquivos() throws IOException, InterruptedException {
		  


		  	String arquivoPDF = "";
		  	String ret = "";
		  	String excluidos  = "";
		  
		  	File file = new File(caminho);
			File afile[] = file.listFiles();
			int i = 0;
			int linha = 0;
			
			for (int j = afile.length; i < j; i++) {
				
				tamanhoLinhas = afile.length;
				
				File arquivos = afile[i];
				arquivoPDF = caminho+arquivos.getName();
				//System.out.println(arquivos.getName());
				
				if(arquivos.getName().indexOf ("pdf") >= 0) {
				
					//Recebe e Le Texto do PDF
					String texto = extraiTextoDoPDF(arquivoPDF);
					
					//Recebe o Conteudo do PDF e coloca em Array com quebra de Linha
					String linhas[] = texto.split("\n");
					
			        					        			
					//Trata o PDF lendo linha a linha do array
					//String textoret = trataPDF(linhas);
					ret = trataPDF(linhas);
					
					 if(Capital)
						 linha = linha + 2;
					 else
						 linha ++;
					
					 //System.out.println("--> "+textoret);
										 
					  if(arquivoPDF.indexOf ("DS_Store") <= 0) //Se arquivo for diferente de arquivo de sistema que nao precisa ser analizado
					  {
						  getSetBB.setPosicaoExiste(linha);
						  
						  EscreverExcel();
							  
						  getSetBB.setPorcentagem((i*100)/afile.length);
							
						  System.out.println(" STATUS: ["+getSetBB.getPorcentagem()+" %]");
						  System.out.println(" LINHA PREENCHIDA ["+i+"]");
					  }
				}
			}
			
			
			getSetBB.setFimArquivo("FINALIZADO COM SUCESSO");
			System.out.println("FINALIZADO COM SUCESSO");
		}
	
  

    		//EXTRAI OS TEXTOS DE DENTRO DO PDF
		  public static String extraiTextoDoPDF(String caminho) {
			  
			  if(caminho.indexOf ("DS_Store") >= 0) {
				  return "NAOePDF";
			  }
			  
			    PDDocument pdfDocument = null;
			    try {
			      pdfDocument = PDDocument.load(caminho);
			      PDFTextStripper stripper = new PDFTextStripper();
			      String texto = stripper.getText(pdfDocument);
			      return texto;
			    } catch (IOException e) {
			      throw new RuntimeException(e);
			    } finally {
			      if (pdfDocument != null) try {
			        pdfDocument.close();
			      } catch (IOException e) {
			        throw new RuntimeException(e);
			      }
			    }
		  }
			  
		  
		  //RECEBE O CONTEUDO DO EXCEL PARA TRATAMENTO DAS INFORMACOES
		  public static String trataPDF(String[] linhas) 
		  {			  
			  	boolean Liquido = false;
			  	IR = false;
			  	Capital = false;
			  
			  	String ret = "";
			  	String valor = "";
			  
			  	DecimalFormat decFormat = new DecimalFormat("0.##");
			  

			  	BigDecimal valorJuros = new BigDecimal(0);
			  	BigDecimal ValorAcomuladoJuros = BigDecimal.ZERO ;
			  
			  	BigDecimal valorValorOrigial = new BigDecimal(0);
			  	BigDecimal ValorAcomuladoValorOrigial = BigDecimal.ZERO ;
			  
      	 		BigDecimal valorCorrecao = new BigDecimal(0);
      	 		BigDecimal ValorAcomuladoCorrecao = BigDecimal.ZERO ;

      	 		BigDecimal valorLiquido = new BigDecimal(0);
      	 		BigDecimal ValorAcomuladoLiquido = BigDecimal.ZERO ;
      	 		
      	 		BigDecimal valorIR = new BigDecimal(0);
      	 		BigDecimal ValorAcomuladoIR = BigDecimal.ZERO ;

      	 		
      	 		//getSetBB.setSaqueOutrosPgto("VAZIO");
      	 		
      	 		getSetBB.setValOriginal("");
      	 		getSetBB.setJuros("");
      	 		getSetBB.setCorrecao("");
      	 		getSetBB.setDataOriginal("");
      	 		getSetBB.setSaqueOutrosPgto("");
      	 		getSetBB.setSaqueCliente("");
      	 		getSetBB.setValIR("");

			  
				  if(banco.equals("IRBB")) // Banco do Brasil
				  {
					  for (int i = 0; i < linhas.length; i++) 
					  {
						  
						 // System.out.println(""+linhas[i] + " ["+i+"]");
						  
						ret = "";
		            		ret = linhas[i];
		            		
		            	//BIG DECIMAL
		            		if(ret.contains("Capital") )
		            		{
				            	String[] partsVara = null;
			            	 	partsVara = ret.split(" ");
			            		
			            	 	//PEGA O VALOR ULTIMO CONTEUDO
			            	 	ret = partsVara[partsVara.length-1];
			            	 	ret = ret.replaceAll("C", "");
			            	 	ret = ret.replaceAll("c", "");
			            	 	ret = ret.replaceAll("D", "");
			            	 	ret = ret.replaceAll("d", "");
			            	 	
			            	 	ret = substituiPonto(ret);
			            	 	
		            			valorValorOrigial = new BigDecimal(ret);
		            			ValorAcomuladoValorOrigial = valorValorOrigial.add(ValorAcomuladoValorOrigial);
		            			
		            			getSetBB.setValOriginal((decFormat.format (ValorAcomuladoValorOrigial).toString()).replace(".", ","));
		            			
		            			Capital = true;

		            			//System.out.println("VALOR CAPITAL SOMADO: "+getSetBB.getValOriginal());		            			
		            		}

			            	//BIG DECIMAL
		            		if(ret.contains("Juros") )
		            		{
				            	String[] partsVara = null;
			            	 	partsVara = ret.split(" ");
			            		
			            	 	//PEGA O VALOR ULTIMO CONTEUDO
			            	 	ret = partsVara[partsVara.length-1];
			            	 	ret = ret.replaceAll("C", "");
			            	 	ret = ret.replaceAll("c", "");
			            	 	ret = ret.replaceAll("D", "");
			            	 	ret = ret.replaceAll("d", "");
			            	 	
			            	 	ret = substituiPonto(ret);
			            	 	
			            	 	valorJuros = new BigDecimal(ret);
			            	 	ValorAcomuladoJuros = valorJuros.add(ValorAcomuladoJuros);
		            			
		            			getSetBB.setJuros((decFormat.format (ValorAcomuladoJuros).toString()).replace(".", ","));
		            			
		            			//System.out.println("VALOR JUROS SOMADO: "+getSetBB.getJuros());

		            		}

			            	//BIG DECIMAL
		            		if(ret.contains("Correção") )
		            		{
				            	String[] partsVara = null;
			            	 	partsVara = ret.split(" ");
			            		
			            	 	//PEGA O VALOR ULTIMO CONTEUDO
			            	 	ret = partsVara[partsVara.length-1];
			            	 	ret = ret.replaceAll("C", "");
			            	 	ret = ret.replaceAll("c", "");
			            	 	ret = ret.replaceAll("D", "");
			            	 	ret = ret.replaceAll("d", "");
			            	 	
			            	 	ret = substituiPonto(ret);
			            	 	
			            	 	valorCorrecao = new BigDecimal(ret);
			            	 	ValorAcomuladoCorrecao = valorCorrecao.add(ValorAcomuladoCorrecao);
		            			
		            			getSetBB.setCorrecao((decFormat.format (ValorAcomuladoCorrecao).toString()).replace(".", ","));
		            			
		            			//System.out.println("VALOR CORRECAO SOMADO: "+getSetBB.getCorrecao());
		            		}

		            		//STRING
		            		if(i == 18) //LINHA 18 PARA PEGAR O MES E O ANO DO EXTRATO
					    {
								ret = linhas[i];
								ret = substituiPonto(ret);
								ret = ret.substring(0, 2)+"/"+ret.substring(2, 4)+"/"+ret.substring(4, 6);
								getSetBB.setDataOriginal(ret);
					    }
		            		
			            	//BIG DECIMAL
		            		if(ret.contains("Liquido") )
		            		{
				            	String[] partsVara = null;
			            	 	partsVara = ret.split(" ");
			            		
			            	 	//PEGA O VALOR ULTIMO CONTEUDO
			            	 	ret = partsVara[partsVara.length-1];
			            	 	ret = ret.replaceAll("C", "");
			            	 	ret = ret.replaceAll("c", "");
			            	 	ret = ret.replaceAll("D", "");
			            	 	ret = ret.replaceAll("d", "");
			            	 	
			            	 	ret = substituiPonto(ret);
			            	 	
			            	 	valorLiquido = new BigDecimal(ret);
			            	 	ValorAcomuladoLiquido = valorLiquido.add(ValorAcomuladoLiquido);
		            						            	 	
			            	 	if(Liquido) {
			            	 		getSetBB.setSaqueOutrosPgto((decFormat.format (ValorAcomuladoLiquido).toString()).replace(".", ","));
			            	 		getSetBB.setSaqueCliente("");
			            	 	}			            	 		
			            	 	else
			            	 		getSetBB.setSaqueCliente((decFormat.format (ValorAcomuladoLiquido).toString()).replace(".", ","));
			            	 		
			            	 	Liquido = true;
			            	 	
			            	 	//System.out.println("VALOR CORRECAO SOMADO: "+getSetBB.getCorrecao());
		            		}		            		

			            	//BIG DECIMAL
		            		if(ret.contains("I.R.") )
		            		{
				            	String[] partsVara = null;
			            	 	partsVara = ret.split(" ");
			            		
			            	 	//PEGA O VALOR ULTIMO CONTEUDO
			            	 	ret = partsVara[partsVara.length-1];
			            	 	ret = ret.replaceAll("C", "");
			            	 	ret = ret.replaceAll("c", "");
			            	 	ret = ret.replaceAll("D", "");
			            	 	ret = ret.replaceAll("d", "");
			            	 	
			            	 	ret = substituiPonto(ret);
			            	 	
			            	 	valorIR = new BigDecimal(ret);
			            	 	ValorAcomuladoIR = valorIR.add(ValorAcomuladoIR);
		            			
		            			getSetBB.setValIR((decFormat.format (ValorAcomuladoIR).toString()).replace(".", ","));
		            			
		            			IR = true;
		            			//System.out.println("VALOR IR SOMADO: "+getSetBB.getValIR());
		            		}	
					  }
				  }	
				  
//				  System.out.println("VALOR CAPITAL : "+getSetBB.getValOriginal());
//				  System.out.println("VALOR JUROS : "+getSetBB.getJuros());
//				  System.out.println("VALOR CORRECAO : "+getSetBB.getCorrecao());
//				  System.out.println("VALOR DATA: "+getSetBB.getDataOriginal());
//				  System.out.println("VALOR LIQUIDO OUTROS PGTO: "+getSetBB.getSaqueOutrosPgto());
//				  System.out.println("VALOR LIQUIDO CLIENTE: "+getSetBB.getSaqueCliente());
//				  System.out.println("VALOR IR : "+getSetBB.getValIR());
				  
				  
				  
				  
			  return ret;
		  }
		  
		  
		  public static String substituiPonto(String str) {
			  
			  	String ret = "";
			  	
			  
			  	Scanner s = new Scanner(str);
				
				String nome = s.nextLine();
				
				for(int i1 = 0; i1 < nome.length(); i1++) 
				{
				 // System.out.println(nome.charAt(i1));
				  
				  
				  if(nome.charAt(i1) == '.')
					  continue;
				  else if (nome.charAt(i1) == ',' )
					  ret = ret+".";
				  else
					ret = ret+nome.charAt(i1);
				}
				
				//System.out.println(ret);
				
	        		return ret;
		    }
		  
		  
		  private static boolean campoNumerico(String campo){           
		        return campo.matches("[0-9]+");   
		}
				  
		  
		 
		 
		  
		  
		  public static String toTitledCase(String nome){
			  
			  //System.out.println("STR ENTRADA= "+ nome);
			  
			  nome = " "+nome; 
			  	
			  String aux =""; // só é utilizada para facilitar 

		        try{ //Bloco try-catch utilizado pois leitura de string gera a exceção abaixo
		            for(int i = 0; i < nome.length(); ++i){
		                if( nome.substring(i, i+1).equals(" ") || nome.substring(i, i+1).equals("  "))
		                {
		                    aux += nome.substring(i+1, i+2).toUpperCase();
		                   // System.out.println("1= "+ aux);
		                }
		                else
		                {
		                    aux += nome.substring(i+1, i+2).toLowerCase();
		                    //System.out.println("2= "+ aux);
		                }
		        }
		        }catch(IndexOutOfBoundsException indexOutOfBoundsException){
		            //não faça nada. só pare tudo e saia do bloco de instrução try-catch
		        }
		        nome = aux;
		       // System.err.println(nome);

			  return nome;
			}  

		  
		


		  
		  
		  public static void EscreverExcel() throws IOException {
			  try{
				  
				  	ZipSecureFile.setMinInflateRatio(-1.0d);	
				  
				  	XSSFWorkbook a = null; 
				  	
			         a = new XSSFWorkbook(new FileInputStream(fo));
			        
			         XSSFSheet my_sheet = null;
			         
			         my_sheet = a.getSheetAt(0);
			        
			        
			        System.out.println("3 -  EXCRECER EXCEL GRAVAR NA LINHA :  " + getSetBB.getPosicaoExiste());
			        

			        //Direita Cor Azul Claro
			        XSSFCellStyle style1 = a.createCellStyle();
			        style1.setAlignment ( XSSFCellStyle.ALIGN_RIGHT ) ; 

			        
			        //Centro Com data Azul
			        CreationHelper createHelper = a.getCreationHelper();
			        XSSFCellStyle data = a.createCellStyle();
			        data.setAlignment ( XSSFCellStyle.ALIGN_CENTER ) ; 
			        data.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yy"));

			        
//					  System.out.println("VALOR CAPITAL : "+getSetBB.getValOriginal());
//					  System.out.println("VALOR JUROS : "+getSetBB.getJuros());
//					  System.out.println("VALOR CORRECAO : "+getSetBB.getCorrecao());
//					  System.out.println("VALOR DATA: "+getSetBB.getDataOriginal());
//					  System.out.println("VALOR LIQUIDO OUTROS PGTO: "+getSetBB.getSaqueOutrosPgto());
//					  System.out.println("VALOR LIQUIDO CLIENTE: "+getSetBB.getSaqueCliente());
//					  System.out.println("VALOR IR : "+getSetBB.getValIR());
//					  System.out.println("GRAVAR NA LINHA : "+getSetBB.getPosicaoExiste());


			        my_sheet.createRow(getSetBB.getPosicaoExiste());

			        
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).createCell(7);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(7).setCellValue(getSetBB.getDataOriginal());
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(7).setCellStyle(data);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(7).setCellType(XSSFCell.CELL_TYPE_STRING);
			        
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).createCell(8);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(8).setCellValue(getSetBB.getValOriginal());
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(8).setCellStyle(style1);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(8).setCellType(XSSFCell.CELL_TYPE_STRING);
			        
			        
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).createCell(1);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(1).setCellValue(getSetBB.getCorrecao());
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(1).setCellStyle(style1);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(1).setCellType(XSSFCell.CELL_TYPE_STRING);
			        
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).createCell(2);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(2).setCellValue(getSetBB.getJuros());
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(2).setCellStyle(style1);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(2).setCellType(XSSFCell.CELL_TYPE_STRING);
			        
			        
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).createCell(3);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(3).setCellValue(getSetBB.getSaqueOutrosPgto());
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(3).setCellStyle(style1);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(3).setCellType(XSSFCell.CELL_TYPE_STRING);
			        
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).createCell(4);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(4).setCellValue(getSetBB.getSaqueCliente());
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(4).setCellStyle(style1);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(4).setCellType(XSSFCell.CELL_TYPE_STRING);
			        
			        
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).createCell(5);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(5).setCellValue(getSetBB.getValIR());
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(5).setCellStyle(style1);
			        my_sheet.getRow(getSetBB.getPosicaoExiste()).getCell(5).setCellType(XSSFCell.CELL_TYPE_STRING);
			        


			        FileOutputStream outputStream  = null;
			        outputStream = new FileOutputStream(new File(excelBB));
			        a.write(outputStream);
			        outputStream.close();//Close in finally if possible
			        outputStream = null;
			        
		        }catch(Exception e){
		        		System.out.println("ATENCAO -  EscreverContaNovaComValor GRAVAR NA LINHA DO EXCEL");
		        		System.out.println(" ERRO ENCONTRAO: " + e);
		        }
			}			  
}
/*******************************************************************************
 * Copyright 2015-16 AutoCognite Testing Research Pvt Ltd
 * 
 * Website: www.AutoCognite.com
 * Email: support [at] autocognite.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.autocognite.arjuna.uiauto.factories;

import org.apache.log4j.Logger;

import com.autocognite.arjuna.enums.FileFormat;
import com.autocognite.arjuna.uiauto.interfaces.PageMapper;
import com.autocognite.arjuna.utils.batteries.FileSystemBatteries;
import com.autocognite.pvt.batteries.config.Batteries;
import com.autocognite.pvt.batteries.exceptions.Problem;
import com.autocognite.pvt.uiautomator.UiAutomator;
import com.autocognite.pvt.uiautomator.lib.IniFilePageMapper;
import com.autocognite.pvt.uiautomator.lib.config.UiAutomatorPropertyType;

public class PageMapperFactory {
	private static Logger sLogger = Logger.getLogger(Batteries.getCentralLogName());
	
	public static PageMapper getFileMapper(String mapPath) throws Exception{
		String ext = FileSystemBatteries.getExtension(mapPath).toUpperCase();
		FileFormat format = null;
		String consideredPath = mapPath;
		try{
			format = FileFormat.valueOf(ext);
		} catch (Exception e){
			throw new Problem(
					"UI Automator", 
					"Page Mapper", 
					"getFileMapper", 
					UiAutomator.problem.UNSUPPORTED_MAP_FILE_FORMAT, 
					Batteries.getProblemText(UiAutomator.problem.UNSUPPORTED_MAP_FILE_FORMAT, ext)
				);			
		}
		
		if (!FileSystemBatteries.isFile(consideredPath)){
			consideredPath = FileSystemBatteries.getCanonicalPath(Batteries.value(UiAutomatorPropertyType.DIRECTORY_UI_MAPS).asString() + "/" + consideredPath);
			if (FileSystemBatteries.isDir(consideredPath)){
				throw new Problem(
						"UI Automator", 
						"Page Mapper", 
						"getFileMapper", 
						UiAutomator.problem.MAPFILE_NOTAFILE, 
						Batteries.getProblemText(UiAutomator.problem.MAPFILE_NOTAFILE, consideredPath)
					);				
			} else if (!FileSystemBatteries.isFile(consideredPath)){
				throw new Problem(
						"UI Automator", 
						"Page Mapper", 
						"getFileMapper", 
						UiAutomator.problem.MAPFILE_NOT_FOUND, 
						Batteries.getProblemText(UiAutomator.problem.MAPFILE_NOT_FOUND, consideredPath)
					);				
			}
		}

		switch(format){
		case INI : return new IniFilePageMapper(consideredPath);
		default: return null;
		}
	}
}

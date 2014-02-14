/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eas.client.Cancellable;
import com.eas.client.CancellableCallbackAdapter;
import com.eas.client.PlatypusLogFormatter;
import com.eas.client.StringCallbackAdapter;
import com.eas.client.Utils;
import com.eas.client.form.Form;
import com.eas.client.form.api.JSContainers;
import com.eas.client.form.api.JSControls;
import com.eas.client.form.api.ModelJSControls;
import com.eas.client.queries.Query;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.shared.event.GroupingHandlerRegistration;

/**
 * 
 * @author mg
 */
public class Application {

	protected static class ElementMaskLoadHandler implements Loader.LoadHandler {
		protected XElement xDiv;

		public ElementMaskLoadHandler(XElement aXDiv) {
			xDiv = aXDiv;
		}

		@Override
		public void started(String anItemName) {
			final String message = "Loading... " + anItemName;
			platypusApplicationLogger.log(Level.INFO, message);
			xDiv.unmask();
			xDiv.mask(message);
		}

		@Override
		public void loaded(String anItemName) {
			final String message = "Loaded " + anItemName;
			platypusApplicationLogger.log(Level.INFO, message);
			xDiv.unmask();
			xDiv.mask(message);
		}
	}

	public static Logger platypusApplicationLogger;
	protected static Map<String, Query> appQueries = new HashMap<String, Query>();
	protected static Loader loader;
	protected static GroupingHandlerRegistration loaderHandlerRegistration = new GroupingHandlerRegistration();

	public static Query getAppQuery(String aQueryId) {
		Query query = appQueries.get(aQueryId);
		if (query != null) {
			AppClient client = query.getClient();
			query = query.copy();
			query.setClient(client);
		}
		return query;
	}

	public static Query putAppQuery(Query aQuery) {
		return appQueries.put(aQuery.getEntityId(), aQuery);
	}

	protected static class ExecuteApplicationCallback extends CancellableCallbackAdapter {

		protected Collection<String> executedAppElements;

		public ExecuteApplicationCallback(Collection<String> appElementsToExecute) {
			super();
			executedAppElements = appElementsToExecute;
		}

		@Override
		protected void doWork() throws Exception {
			loaderHandlerRegistration.removeHandler();
			for (String appElementName : executedAppElements) {
				Form f = getStartForm(appElementName);
				RootPanel target = RootPanel.get(appElementName);
				if (target != null) {
					target.getElement().<XElement> cast().unmask();
					if (f != null) {
						f.showOnPanel(target);
					} else {
						target.getElement().setInnerHTML(loader.getAppElementError(appElementName));
					}
				} else {
					if (f != null) {
						f.show(false, null, null);
					} else {
						JavaScriptObject module = getModule(appElementName);
					}
				}
			}
			onReady();
		}
	}

	protected native static JavaScriptObject getModule(String appElement)/*-{
		return $wnd.Modules.get(appElement);
	}-*/;

	public native static Form getStartForm(String appElement)/*-{
		var existingModule = $wnd.Modules.get(appElement);
		return existingModule["x-Form"];
	}-*/;

	/**
	 * This method is publicONLY because of tests!
	 * 
	 * @param aClient
	 * @throws Exception
	 */
	public native static void publish(AppClient aClient) throws Exception /*-{
	
		$wnd.Function.prototype.invokeLater = function() {
			var _func = this;
			var _arguments = arguments;
			@com.eas.client.Utils::invokeLater(Lcom/google/gwt/core/client/JavaScriptObject;)(function(){
				_func.apply(_func, _arguments);
			});
		}
		
		$wnd.Function.prototype.invokeDelayed = function() {
			var _func = this;
			var _arguments = arguments;
		    if (!_arguments || !_arguments.length || _arguments.length < 1)
		        throw "schedule needs at least 1 argument - timeout value.";
		    var userArgs = [];
		    for (var i = 1; i < _arguments.length; i++) {
		        userArgs.push(_arguments[i]);
		    }
			@com.eas.client.Utils::invokeScheduled(ILcom/google/gwt/core/client/JavaScriptObject;)(_arguments[0], function(){
				try{
					_func.apply(_func, userArgs);
				}catch(e){
					$wnd.Logger.severe(e);
				}
			});
		}
		
		$wnd.selectFile = function(aCallback) {
			@com.eas.client.gxtcontrols.ControlsUtils::jsSelectFile(Lcom/google/gwt/core/client/JavaScriptObject;)(aCallback);
		}
		
		$wnd.Resource = {};
		Object.defineProperty($wnd.Resource, "upload", {get : function(){
				return function(aFile, aCompleteCallback, aProgressCallback, aAbortCallback) {
					return @com.eas.client.application.AppClient::jsUpload(Lcom/eas/client/published/PublishedFile;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;)(aFile, aCompleteCallback, aProgressCallback, aAbortCallback);
				}
		}});
		Object.defineProperty($wnd.Resource, "load", {get : function(){
		        return function(aResName, onSuccess, onFailure){
	            	return $wnd.boxAsJs(@com.eas.client.application.AppClient::jsLoad(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Z)(aResName, onSuccess, onFailure, false));
		        };
		}});
		
		Object.defineProperty($wnd.Resource, "loadText", {get : function(){
		        return function(aResName, aOnSuccessOrEncoding, aOnSuccessOrOnFailure, aOnFailure){
		        	var onSuccess = aOnSuccessOrEncoding;
		        	var onFailure = aOnSuccessOrOnFailure;
		        	if(typeof onSuccess != "function"){
		        		onSuccess = aOnSuccessOrOnFailure;
		        		onFailure = aOnFailure;
		        	}
		        	return $wnd.boxAsJs(@com.eas.client.application.AppClient::jsLoad(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Z)(aResName, onSuccess, onFailure, true));
		        };
		}});
		
		$wnd.logout = function(onSuccess, onFailure) {
			return @com.eas.client.application.AppClient::jsLogout(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;)(onSuccess, onFailure);
		}
		
		$wnd.getElementComputedStyle = function(_elem) {
			if (typeof _elem.currentStyle != 'undefined')
			{
				return _elem.currentStyle; 
		    } else {
		    	return document.defaultView.getComputedStyle(_elem, null); 
		    }			
		}
	
		$wnd.boxAsJs = function(aValue) {
			if(aValue == null)
				return null;
			else if(@com.eas.client.Utils::isNumber(Ljava/lang/Object;)(aValue))
				return aValue.@java.lang.Number::doubleValue()();
			else if(@com.eas.client.Utils::isBoolean(Ljava/lang/Object;)(aValue))
				return aValue.@java.lang.Boolean::booleanValue()();
			else // dates, strings, complex java objects handled in Utils.toJs()
				return aValue;
		}
		
		$wnd.boxAsJava = function (aValue) {
			var valueType = typeof(aValue);
			if(valueType == "string")
				return aValue;
			else if(valueType == "number")
				return @java.lang.Double::new(D)(aValue);
			else if(valueType == "boolean")
				return @java.lang.Boolean::new(Z)(aValue);
			else // dates, strings, complex js objects handled in Utils.toJava() 
				return aValue;
		}
		
        var hexcase = 0;   
        var b64pad  = "";  

        function hex_md5(s)    {
            return rstr2hex(rstr_md5(str2rstr_utf8(s)));
        }
        function b64_md5(s)    {
            return rstr2b64(rstr_md5(str2rstr_utf8(s)));
        }
        function any_md5(s, e) {
            return rstr2any(rstr_md5(str2rstr_utf8(s)), e);
        }
        function hex_hmac_md5(k, d)
        {
            return rstr2hex(rstr_hmac_md5(str2rstr_utf8(k), str2rstr_utf8(d)));
        }
        function b64_hmac_md5(k, d)
        {
            return rstr2b64(rstr_hmac_md5(str2rstr_utf8(k), str2rstr_utf8(d)));
        }
        function any_hmac_md5(k, d, e)
        {
            return rstr2any(rstr_hmac_md5(str2rstr_utf8(k), str2rstr_utf8(d)), e);
        }

        function md5_vm_test()
        {
            return hex_md5("abc").toLowerCase() == "900150983cd24fb0d6963f7d28e17f72";
        }

        function rstr_md5(s)
        {
            return binl2rstr(binl_md5(rstr2binl(s), s.length * 8));
        }

        function rstr_hmac_md5(key, data)
        {
            var bkey = rstr2binl(key);
            if(bkey.length > 16) bkey = binl_md5(bkey, key.length * 8);

            var ipad = Array(16), opad = Array(16);
            for(var i = 0; i < 16; i++)
            {
                ipad[i] = bkey[i] ^ 0x36363636;
                opad[i] = bkey[i] ^ 0x5C5C5C5C;
            }

            var hash = binl_md5(ipad.concat(rstr2binl(data)), 512 + data.length * 8);
            return binl2rstr(binl_md5(opad.concat(hash), 512 + 128));
        }

        function rstr2hex(input)
        {
            try {
                hexcase
            } catch(e) {
                hexcase=0;
            }
            var hex_tab = hexcase ? "0123456789ABCDEF" : "0123456789abcdef";
            var output = "";
            var x;
            for(var i = 0; i < input.length; i++)
            {
                x = input.charCodeAt(i);
                output += hex_tab.charAt((x >>> 4) & 0x0F)
                +  hex_tab.charAt( x        & 0x0F);
            }
            return output;
        }

        function rstr2b64(input)
        {
            try {
                b64pad
            } catch(e) {
                b64pad='';
            }
            var tab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
            var output = "";
            var len = input.length;
            for(var i = 0; i < len; i += 3)
            {
                var triplet = (input.charCodeAt(i) << 16)
                | (i + 1 < len ? input.charCodeAt(i+1) << 8 : 0)
                | (i + 2 < len ? input.charCodeAt(i+2)      : 0);
                for(var j = 0; j < 4; j++)
                {
                    if(i * 8 + j * 6 > input.length * 8) output += b64pad;
                    else output += tab.charAt((triplet >>> 6*(3-j)) & 0x3F);
                }
            }
            return output;
        }

        function rstr2any(input, encoding)
        {
            var divisor = encoding.length;
            var i, j, q, x, quotient;

            var dividend = Array(Math.ceil(input.length / 2));
            for(i = 0; i < dividend.length; i++)
            {
                dividend[i] = (input.charCodeAt(i * 2) << 8) | input.charCodeAt(i * 2 + 1);
            }

            var full_length = Math.ceil(input.length * 8 /
                (Math.log(encoding.length) / Math.log(2)));
            var remainders = Array(full_length);
            for(j = 0; j < full_length; j++)
            {
                quotient = Array();
                x = 0;
                for(i = 0; i < dividend.length; i++)
                {
                    x = (x << 16) + dividend[i];
                    q = Math.floor(x / divisor);
                    x -= q * divisor;
                    if(quotient.length > 0 || q > 0)
                        quotient[quotient.length] = q;
                }
                remainders[j] = x;
                dividend = quotient;
            }

            var output = "";
            for(i = remainders.length - 1; i >= 0; i--)
                output += encoding.charAt(remainders[i]);

            return output;
        }

        function str2rstr_utf8(input)
        {
            var output = "";
            var i = -1;
            var x, y;

            while(++i < input.length)
            {
                x = input.charCodeAt(i);
                y = i + 1 < input.length ? input.charCodeAt(i + 1) : 0;
                if(0xD800 <= x && x <= 0xDBFF && 0xDC00 <= y && y <= 0xDFFF)
                {
                    x = 0x10000 + ((x & 0x03FF) << 10) + (y & 0x03FF);
                    i++;
                }

                if(x <= 0x7F)
                    output += String.fromCharCode(x);
                else if(x <= 0x7FF)
                    output += String.fromCharCode(0xC0 | ((x >>> 6 ) & 0x1F),
                        0x80 | ( x         & 0x3F));
                else if(x <= 0xFFFF)
                    output += String.fromCharCode(0xE0 | ((x >>> 12) & 0x0F),
                        0x80 | ((x >>> 6 ) & 0x3F),
                        0x80 | ( x         & 0x3F));
                else if(x <= 0x1FFFFF)
                    output += String.fromCharCode(0xF0 | ((x >>> 18) & 0x07),
                        0x80 | ((x >>> 12) & 0x3F),
                        0x80 | ((x >>> 6 ) & 0x3F),
                        0x80 | ( x         & 0x3F));
            }
            return output;
        }

        function str2rstr_utf16le(input)
        {
            var output = "";
            for(var i = 0; i < input.length; i++)
                output += String.fromCharCode( input.charCodeAt(i)        & 0xFF,
                    (input.charCodeAt(i) >>> 8) & 0xFF);
            return output;
        }

        function str2rstr_utf16be(input)
        {
            var output = "";
            for(var i = 0; i < input.length; i++)
                output += String.fromCharCode((input.charCodeAt(i) >>> 8) & 0xFF,
                    input.charCodeAt(i)        & 0xFF);
            return output;
        }

        function rstr2binl(input)
        {
            var output = Array(input.length >> 2);
            for(var i = 0; i < output.length; i++)
                output[i] = 0;
            for(var i = 0; i < input.length * 8; i += 8)
                output[i>>5] |= (input.charCodeAt(i / 8) & 0xFF) << (i%32);
            return output;
        }

        function binl2rstr(input)
        {
            var output = "";
            for(var i = 0; i < input.length * 32; i += 8)
                output += String.fromCharCode((input[i>>5] >>> (i % 32)) & 0xFF);
            return output;
        }

        function binl_md5(x, len)
        {
            x[len >> 5] |= 0x80 << ((len) % 32);
            x[(((len + 64) >>> 9) << 4) + 14] = len;

            var a =  1732584193;
            var b = -271733879;
            var c = -1732584194;
            var d =  271733878;

            for(var i = 0; i < x.length; i += 16)
            {
                var olda = a;
                var oldb = b;
                var oldc = c;
                var oldd = d;

                a = md5_ff(a, b, c, d, x[i+ 0], 7 , -680876936);
                d = md5_ff(d, a, b, c, x[i+ 1], 12, -389564586);
                c = md5_ff(c, d, a, b, x[i+ 2], 17,  606105819);
                b = md5_ff(b, c, d, a, x[i+ 3], 22, -1044525330);
                a = md5_ff(a, b, c, d, x[i+ 4], 7 , -176418897);
                d = md5_ff(d, a, b, c, x[i+ 5], 12,  1200080426);
                c = md5_ff(c, d, a, b, x[i+ 6], 17, -1473231341);
                b = md5_ff(b, c, d, a, x[i+ 7], 22, -45705983);
                a = md5_ff(a, b, c, d, x[i+ 8], 7 ,  1770035416);
                d = md5_ff(d, a, b, c, x[i+ 9], 12, -1958414417);
                c = md5_ff(c, d, a, b, x[i+10], 17, -42063);
                b = md5_ff(b, c, d, a, x[i+11], 22, -1990404162);
                a = md5_ff(a, b, c, d, x[i+12], 7 ,  1804603682);
                d = md5_ff(d, a, b, c, x[i+13], 12, -40341101);
                c = md5_ff(c, d, a, b, x[i+14], 17, -1502002290);
                b = md5_ff(b, c, d, a, x[i+15], 22,  1236535329);

                a = md5_gg(a, b, c, d, x[i+ 1], 5 , -165796510);
                d = md5_gg(d, a, b, c, x[i+ 6], 9 , -1069501632);
                c = md5_gg(c, d, a, b, x[i+11], 14,  643717713);
                b = md5_gg(b, c, d, a, x[i+ 0], 20, -373897302);
                a = md5_gg(a, b, c, d, x[i+ 5], 5 , -701558691);
                d = md5_gg(d, a, b, c, x[i+10], 9 ,  38016083);
                c = md5_gg(c, d, a, b, x[i+15], 14, -660478335);
                b = md5_gg(b, c, d, a, x[i+ 4], 20, -405537848);
                a = md5_gg(a, b, c, d, x[i+ 9], 5 ,  568446438);
                d = md5_gg(d, a, b, c, x[i+14], 9 , -1019803690);
                c = md5_gg(c, d, a, b, x[i+ 3], 14, -187363961);
                b = md5_gg(b, c, d, a, x[i+ 8], 20,  1163531501);
                a = md5_gg(a, b, c, d, x[i+13], 5 , -1444681467);
                d = md5_gg(d, a, b, c, x[i+ 2], 9 , -51403784);
                c = md5_gg(c, d, a, b, x[i+ 7], 14,  1735328473);
                b = md5_gg(b, c, d, a, x[i+12], 20, -1926607734);

                a = md5_hh(a, b, c, d, x[i+ 5], 4 , -378558);
                d = md5_hh(d, a, b, c, x[i+ 8], 11, -2022574463);
                c = md5_hh(c, d, a, b, x[i+11], 16,  1839030562);
                b = md5_hh(b, c, d, a, x[i+14], 23, -35309556);
                a = md5_hh(a, b, c, d, x[i+ 1], 4 , -1530992060);
                d = md5_hh(d, a, b, c, x[i+ 4], 11,  1272893353);
                c = md5_hh(c, d, a, b, x[i+ 7], 16, -155497632);
                b = md5_hh(b, c, d, a, x[i+10], 23, -1094730640);
                a = md5_hh(a, b, c, d, x[i+13], 4 ,  681279174);
                d = md5_hh(d, a, b, c, x[i+ 0], 11, -358537222);
                c = md5_hh(c, d, a, b, x[i+ 3], 16, -722521979);
                b = md5_hh(b, c, d, a, x[i+ 6], 23,  76029189);
                a = md5_hh(a, b, c, d, x[i+ 9], 4 , -640364487);
                d = md5_hh(d, a, b, c, x[i+12], 11, -421815835);
                c = md5_hh(c, d, a, b, x[i+15], 16,  530742520);
                b = md5_hh(b, c, d, a, x[i+ 2], 23, -995338651);

                a = md5_ii(a, b, c, d, x[i+ 0], 6 , -198630844);
                d = md5_ii(d, a, b, c, x[i+ 7], 10,  1126891415);
                c = md5_ii(c, d, a, b, x[i+14], 15, -1416354905);
                b = md5_ii(b, c, d, a, x[i+ 5], 21, -57434055);
                a = md5_ii(a, b, c, d, x[i+12], 6 ,  1700485571);
                d = md5_ii(d, a, b, c, x[i+ 3], 10, -1894986606);
                c = md5_ii(c, d, a, b, x[i+10], 15, -1051523);
                b = md5_ii(b, c, d, a, x[i+ 1], 21, -2054922799);
                a = md5_ii(a, b, c, d, x[i+ 8], 6 ,  1873313359);
                d = md5_ii(d, a, b, c, x[i+15], 10, -30611744);
                c = md5_ii(c, d, a, b, x[i+ 6], 15, -1560198380);
                b = md5_ii(b, c, d, a, x[i+13], 21,  1309151649);
                a = md5_ii(a, b, c, d, x[i+ 4], 6 , -145523070);
                d = md5_ii(d, a, b, c, x[i+11], 10, -1120210379);
                c = md5_ii(c, d, a, b, x[i+ 2], 15,  718787259);
                b = md5_ii(b, c, d, a, x[i+ 9], 21, -343485551);

                a = safe_add(a, olda);
                b = safe_add(b, oldb);
                c = safe_add(c, oldc);
                d = safe_add(d, oldd);
            }
            return Array(a, b, c, d);
        }

        function md5_cmn(q, a, b, x, s, t)
        {
            return safe_add(bit_rol(safe_add(safe_add(a, q), safe_add(x, t)), s),b);
        }
        function md5_ff(a, b, c, d, x, s, t)
        {
            return md5_cmn((b & c) | ((~b) & d), a, b, x, s, t);
        }
        function md5_gg(a, b, c, d, x, s, t)
        {
            return md5_cmn((b & d) | (c & (~d)), a, b, x, s, t);
        }
        function md5_hh(a, b, c, d, x, s, t)
        {
            return md5_cmn(b ^ c ^ d, a, b, x, s, t);
        }
        function md5_ii(a, b, c, d, x, s, t)
        {
            return md5_cmn(c ^ (b | (~d)), a, b, x, s, t);
        }

        function safe_add(x, y)
        {
            var lsw = (x & 0xFFFF) + (y & 0xFFFF);
            var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
            return (msw << 16) | (lsw & 0xFFFF);
        }

        function bit_rol(num, cnt)
        {
            return (num << cnt) | (num >>> (32 - cnt));
        }

		if(!$wnd.platypus)
			$wnd.platypus = {};
		$wnd.platypus.getCached = function(appElementId) {
			return aClient.@com.eas.client.application.AppClient::getCachedAppElement(Ljava/lang/String;)(appElementId);
		};
		$wnd.platypus.readModel = function(appElementDoc, aModule) {
			var nativeModel = @com.eas.client.model.store.XmlDom2Model::transform(Lcom/google/gwt/xml/client/Document;Lcom/google/gwt/core/client/JavaScriptObject;)(appElementDoc, aModule);
			nativeModel.@com.eas.client.model.Model::publish(Lcom/google/gwt/core/client/JavaScriptObject;)(aModule);
			return nativeModel;
		};
		$wnd.platypus.readForm = function(appElementDoc, aModule) {
			var nativeModel = aModule.model.unwrap();
			var nativeForm = @com.eas.client.form.store.XmlDom2Form::transform(Lcom/google/gwt/xml/client/Document;Lcom/eas/client/model/Model;)(appElementDoc, nativeModel);
			nativeForm.@com.eas.client.form.Form::publish(Lcom/google/gwt/core/client/JavaScriptObject;)(aModule);
			return nativeForm;
		};
		$wnd.platypus.HTML5 = "Html5 client";
		$wnd.platypus.J2SE = "Java SE client";
		$wnd.platypus.agent = $wnd.platypus.HTML5; 
		function _Modules() {
			var platypusModules = {};
			this.get = function(aModuleId) {
				var pModule = platypusModules[aModuleId];
				if (pModule == null || pModule == undefined) {
					if(!$wnd.platypusModulesConstructors) {
						throw 'No $wnd.platypusModulesConstructors';
					}
					var mc = $wnd.platypusModulesConstructors[aModuleId];
					if (mc != null && mc != undefined) {
						pModule = new mc();
						platypusModules[aModuleId] = pModule;
					} else
						throw 'No module constructor to module: ' + aModuleId;
				}
				return pModule;
			}
		}
		$wnd.Modules = new _Modules();
		$wnd.Module = function(aModuleId)
		{
			var mc = $wnd.platypusModulesConstructors[aModuleId];
			if (mc){
				mc.call(this);
			} else
				throw 'No module constructor to module: ' + aModuleId;
		};
		$wnd.Form = $wnd.Module;
		$wnd.Form.getShownForm = function(aFormKey){
			return @com.eas.client.form.Form::getShownForm(Ljava/lang/String;)(aFormKey);
		}
		$wnd.ServerModule = function(aModuleId){
			$wnd.platypus.defineServerModule(aModuleId, this);
		};
		$wnd.ServerReport = function(aModuleId){
			$wnd.platypus.defineServerModule(aModuleId, this);
		};
		$wnd.Report = $wnd.ServerReport;
		
		Object.defineProperty($wnd.Form, "shown", {
			get : function() {
				return @com.eas.client.form.Form::getShownForms()();
			}
		});
		Object.defineProperty($wnd.Form, "onChange", {
			get : function() {
				return @com.eas.client.form.Form::getOnChange()();
			},
			set : function(aValue) {
				@com.eas.client.form.Form::setOnChange(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
			}
		});
		$wnd.require = function (aDeps, aOnSuccess, aOnFailure) {
			var deps = Array.isArray(aDeps) ? aDeps : [aDeps];
			@com.eas.client.application.Application::require(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;)(deps, aOnSuccess, aOnFailure);
		} 
		function _Icons() {
			this.load = function(aIconName) {
				var appClient = @com.eas.client.application.AppClient::getInstance()();
				return appClient.@com.eas.client.application.AppClient::getImageResource(Ljava/lang/String;)(aIconName != null ? '' + aIconName : null);
			}
		}
		$wnd.Icon = new _Icons();
		$wnd.Icons = $wnd.Icon;
		function _Color(aRed, aGreen, aBlue, aAlpha){
			var _red = 0, _green = 0, _blue = 0, _alpha = 0xff;
			if(arguments.length == 1){
				var _color = @com.eas.client.gxtcontrols.ControlsUtils::parseColor(Ljava/lang/String;)(aRed + '');
				if(_color){
					_red = _color.red;
					_green = _color.green;
					_blue = _color.blue;
				}else
					throw "String like \"#cfcfcf\" is expected.";
			}else if(arguments.length >= 3){
				if(aRed)
					_red = aRed;
				if(aGreen)
					_green = aGreen;
				if(aBlue)
					_blue = aBlue;
				if(aAlpha)
					_alpha = aAlpha;
			}else{
				throw "String like \"#cfcfcf\" or three color components with optional alpha is expected.";
			}
			var _self = this;
			Object.defineProperty(_self, "red", { get:function(){ return _red;} });
			Object.defineProperty(_self, "green", { get:function(){ return _green;} });
			Object.defineProperty(_self, "blue", { get:function(){ return _blue;} });
			Object.defineProperty(_self, "alpha", { get:function(){ return _alpha;} });
			_self.toStyled = function(){
				return "rgba("+_self.red+","+_self.green+","+_self.blue+","+_self.alpha/255.0+")"; 
			}
			_self.toString = function(){
				var sred = (new Number(_self.red)).toString(16);
				if(sred.length == 1)
					sred = "0"+sred;
				var sgreen = (new Number(_self.green)).toString(16);
				if(sgreen.length == 1)
					sgreen = "0"+sgreen;
				var sblue = (new Number(_self.blue)).toString(16);
				if(sblue.length == 1)
					sblue = "0"+sblue;
				return "#"+sred+sgreen+sblue;
			}
		}; 
		$wnd.Color = _Color;
		$wnd.Color.black = new $wnd.Color(0,0,0);
		$wnd.Color.BLACK = new $wnd.Color(0,0,0);
		$wnd.Color.blue = new $wnd.Color(0,0,0xff);
		$wnd.Color.BLUE = new $wnd.Color(0,0,0xff);
		$wnd.Color.cyan = new $wnd.Color(0,0xff,0xff);
		$wnd.Color.CYAN = new $wnd.Color(0,0xff,0xff);
		$wnd.Color.DARK_GRAY = new $wnd.Color(0x40, 0x40, 0x40);
		$wnd.Color.darkGray = new $wnd.Color(0x40, 0x40, 0x40);
		$wnd.Color.gray = new $wnd.Color(0x80, 0x80, 0x80);
		$wnd.Color.GRAY = new $wnd.Color(0x80, 0x80, 0x80);
		$wnd.Color.green = new $wnd.Color(0, 0xff, 0);
		$wnd.Color.GREEN = new $wnd.Color(0, 0xff, 0);
		$wnd.Color.LIGHT_GRAY = new $wnd.Color(0xC0, 0xC0, 0xC0);
		$wnd.Color.lightGray = new $wnd.Color(0xC0, 0xC0, 0xC0);
		$wnd.Color.magenta = new $wnd.Color(0xff, 0, 0xff);
		$wnd.Color.MAGENTA = new $wnd.Color(0xff, 0, 0xff);
		$wnd.Color.orange = new $wnd.Color(0xff, 0xC8, 0);
		$wnd.Color.ORANGE = new $wnd.Color(0xff, 0xC8, 0);
		$wnd.Color.pink = new $wnd.Color(0xFF, 0xAF, 0xAF);
		$wnd.Color.PINK = new $wnd.Color(0xFF, 0xAF, 0xAF);
		$wnd.Color.red = new $wnd.Color(0xFF, 0, 0);
		$wnd.Color.RED = new $wnd.Color(0xFF, 0, 0);
		$wnd.Color.white = new $wnd.Color(0xFF, 0xff, 0xff);
		$wnd.Color.WHITE = new $wnd.Color(0xFF, 0xff, 0xff);
		$wnd.Color.yellow = new $wnd.Color(0xFF, 0xff, 0);
		$wnd.Color.YELLOW = new $wnd.Color(0xFF, 0xff, 0);
		$wnd.Colors = $wnd.Color;
		
		function _Font(aFamily, aStyle, aSize){
			var _self = this;
			Object.defineProperty(_self, "family", { get:function(){ return aFamily;} });
			Object.defineProperty(_self, "style", { get:function(){ return aStyle;} });
			Object.defineProperty(_self, "size", { get:function(){ return aSize;} });			
		}; 
		$wnd.Font = _Font;
		$wnd.Cursor = {
		    CROSSHAIR : "crosshair",
		    DEFAULT : "default",
		    AUTO : "auto",
		    E_RESIZE : "e-resize",
		    // help ?
		    // progress ?
		    HAND : "pointer",
		    MOVE : "move",
		    NE_RESIZE : "ne-resize",
		    NW_RESIZE : "nw-resize",
		    N_RESIZE : "n-resize",
		    SE_RESIZE : "se-resize",
		    SW_RESIZE : "sw-resize",
		    S_RESIZE : "s-resize",
		    TEXT : "text",
		    WAIT : "wait",
		    W_RESIZE : "w-resize"
		};
		$wnd.IDGenerator = {
			genID : function()
			{
				return @com.bearsoft.rowset.utils.IDGenerator::genId()();
			}
		};
		$wnd.MD5Generator = {
			generate : function(aSource)
			{
				return hex_md5(aSource).toLowerCase();
			}
		};
		$wnd.Logger = new (function(){
			var nativeLogger = @com.eas.client.application.Application::platypusApplicationLogger;
			this.severe = function(aMessage){
				nativeLogger.@java.util.logging.Logger::severe(Ljava/lang/String;)(aMessage!=null?""+aMessage:null);
			}
			this.warning = function(aMessage){
				nativeLogger.@java.util.logging.Logger::warning(Ljava/lang/String;)(aMessage!=null?""+aMessage:null);
			}
			this.info = function(aMessage){
				nativeLogger.@java.util.logging.Logger::info(Ljava/lang/String;)(aMessage!=null?""+aMessage:null);
			}
			this.fine = function(aMessage){
				nativeLogger.@java.util.logging.Logger::fine(Ljava/lang/String;)(aMessage!=null?""+aMessage:null);
			}
			this.finer = function(aMessage){
				nativeLogger.@java.util.logging.Logger::finer(Ljava/lang/String;)(aMessage!=null?""+aMessage:null);
			}
			this.finest = function(aMessage){
				nativeLogger.@java.util.logging.Logger::finest(Ljava/lang/String;)(aMessage!=null?""+aMessage:null);
			}
		})();
		function _Style(aParent)
		{
			var _self = this;
			_self.parent = null;
			if(aParent)
				_self.parent = aParent;
			var _background = null;
			var _foreground = null;
		    var _font = null;
		    var _align = null;
		    var _icon = null;
		    var _folderIcon = null;
		    var _openFolderIcon = null;
		    var _leafIcon = null;
		    Object.defineProperty(_self, "background", {
		    	get : function(){
		    		if(_background == null && _self.parent != null)
		    			return _self.parent.background;
		    		else
		    			return _background;
		    	},
		    	set : function(aValue){
		    		_background = aValue;
		    	}
		    });
		    Object.defineProperty(_self, "foreground", {
		    	get : function(){
		    		if(_foreground == null && _self.parent != null)
		    			return _self.parent.foreground;
		    		else
		    			return _foreground;
		    	},
		    	set : function(aValue){
		    		_foreground = aValue;
		    	}
		    });
		    Object.defineProperty(_self, "font", {
		    	get : function(){
		    		if(_font == null && _self.parent != null)
		    			return _self.parent.font;
		    		else
			    		return _font;
		    	},
		    	set : function(aValue){
		    		_font = aValue;
		    	}
		    });
		    Object.defineProperty(_self, "align", {
		    	get : function(){
		    		if(_align == null && _self.parent != null)
		    			return _self.parent.align;
		    		else
			    		return _align;
		    	},
		    	set : function(aValue){
		    		_align = aValue;
		    	}
		    });
		    Object.defineProperty(_self, "icon", {
		    	get : function(){
		    		if(_icon == null && _self.parent != null)
		    			return _self.parent.icon;
		    		else
			    		return _icon;
		    	},
		    	set : function(aValue){
		    		_icon = aValue;
		    	}
		    });
		    Object.defineProperty(_self, "folderIcon", {
		    	get : function(){
		    		if(_folderIcon == null && _self.parent != null)
		    			return _self.parent.folderIcon;
		    		else
			    		return _folderIcon;
		    	},
		    	set : function(aValue){
		    		_folderIcon = aValue;
		    	}
		    });
		    Object.defineProperty(_self, "openFolderIcon", {
		    	get : function(){
		    		if(_openFolderIcon == null && _self.parent != null)
		    			return _self.parent.openFolderIcon;
		    		else
			    		return _openFolderIcon;
		    	},
		    	set : function(aValue){
		    		_openFolderIcon = aValue;
		    	}
		    });
		    Object.defineProperty(_self, "leafIcon", {
		    	get : function(){
		    		if(_leafIcon == null && _self.parent != null)
		    			return _self.parent.leafIcon;
		    		else
			    		return _leafIcon;
		    	},
		    	set : function(aValue){
		    		_leafIcon = aValue;
		    	}
		    });
		}
		$wnd.Style = _Style;
		function _LineChart(chartTitle, xTitle, yTitle, pDs){
			var _dataSource = pDs;
        	var nativeChart = @com.eas.client.chart.LineChart::new(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)(chartTitle, xTitle, yTitle, pDs.unwrap ? pDs.unwrap() : pDs);
			this.addSeries = function(pXAxisField, pYAxisField, pTitle){
				nativeChart.@com.eas.client.chart.LineChart::addSeries(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(pXAxisField, pYAxisField, pTitle);
			}
			this.unwrap = function(){
				return nativeChart; 
			}
			this.dataWillChange = function(){
				nativeChart.@com.eas.client.chart.LineChart::dataWillChange()();
			}
			this.dataChanged = function(){
				nativeChart.@com.eas.client.chart.LineChart::dataChanged()();
			}
			Object.defineProperty(this, "data", {
				get: function(){
					return _dataSource;
				},
				set: function(aValue){
					if(_dataSource != aValue){
					    nativeChart.@com.eas.client.chart.LineChart::changeDataSource(Ljava/lang/Object;)(aValue.unwrap ? aValue.unwrap() : aValue);
				        _dataSource = aValue;
					}
				}
			});
			nativeChart.@com.eas.client.chart.AbstractChart::setJsPublished(Lcom/eas/client/gxtcontrols/published/PublishedComponent;)(this);
			return this; 
		};
		$wnd.LineChart = _LineChart;
		function _TimeSeriesChart(chartTitle, xTitle, yTitle, pDs){
			var _dataSource = pDs;
        	var nativeChart = @com.eas.client.chart.TimeSeriesChart::new(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)(chartTitle, xTitle, yTitle, pDs.unwrap ? pDs.unwrap() : pDs);
			this.addSeries = function(pXAxisField, pYAxisField, pTitle){
				nativeChart.@com.eas.client.chart.TimeSeriesChart::addSeries(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(pXAxisField, pYAxisField, pTitle);
			}
			this.unwrap = function(){
				return nativeChart; 
			}
			this.dataWillChange = function(){
				nativeChart.@com.eas.client.chart.TimeSeriesChart::dataWillChange()();
			}
			this.dataChanged = function(){
				nativeChart.@com.eas.client.chart.TimeSeriesChart::dataChanged()();
			}
			Object.defineProperty(this, "data", {
				get: function(){
					return _dataSource;
				},
				set: function(aValue){
					nativeChart.@com.eas.client.chart.TimeSeriesChart::changeDataSource(Ljava/lang/Object;)(aValue.unwrap ? aValue.unwrap() : aValue);
					_dataSource = aValue;
				}
			});
			Object.defineProperty(this, "XLabelsFormat", {
				get: function(){
					return nativeChart.@com.eas.client.chart.TimeSeriesChart::getXLabelsFormat();
				},
				set: function(aValue){
					nativeChart.@com.eas.client.chart.TimeSeriesChart::setXLabelsFormat(Ljava/lang/String;)(aValue);
				}
			});
			nativeChart.@com.eas.client.chart.AbstractChart::setJsPublished(Lcom/eas/client/gxtcontrols/published/PublishedComponent;)(this);
			return this; 
		};
		$wnd.TimeSeriesChart = _TimeSeriesChart;
		function _PieChart(pTitle, pXAxisField, pYAxisField, pDs){
			var _dataSource = pDs;
        	var nativeChart = @com.eas.client.chart.PieChart::new(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)(pTitle, pXAxisField, pYAxisField, pDs.unwrap ? pDs.unwrap() : pDs);
			this.unwrap = function(){
				return nativeChart; 
			}
			this.dataWillChange = function(){
				nativeChart.@com.eas.client.chart.PieChart::dataWillChange()();
			}
			this.dataChanged = function(){
				nativeChart.@com.eas.client.chart.PieChart::dataChanged()();
			}
			Object.defineProperty(this, "data", {
				get: function(){
					return _dataSource;
				},
				set: function(aValue){
					nativeChart.@com.eas.client.chart.PieChart::changeDataSource(Ljava/lang/Object;)(aValue.unwrap ? aValue.unwrap() : aValue);
					_dataSource = aValue;
				}
			});
			nativeChart.@com.eas.client.chart.AbstractChart::setJsPublished(Lcom/eas/client/gxtcontrols/published/PublishedComponent;)(this);
			return this; 
		};
		$wnd.PieChart = _PieChart;
		
	    $wnd.VK_ALT = @com.google.gwt.event.dom.client.KeyCodes::KEY_ALT;
	    $wnd.VK_BACKSPACE = @com.google.gwt.event.dom.client.KeyCodes::KEY_BACKSPACE;
	    $wnd.VK_BACKSPACE = @com.google.gwt.event.dom.client.KeyCodes::KEY_BACKSPACE;
	    $wnd.VK_DELETE = @com.google.gwt.event.dom.client.KeyCodes::KEY_DELETE;
	    $wnd.VK_DOWN = @com.google.gwt.event.dom.client.KeyCodes::KEY_DOWN;
	    $wnd.VK_END = @com.google.gwt.event.dom.client.KeyCodes::KEY_END;
	    $wnd.VK_ENTER = @com.google.gwt.event.dom.client.KeyCodes::KEY_ENTER;
	    $wnd.VK_ESCAPE = @com.google.gwt.event.dom.client.KeyCodes::KEY_ESCAPE;
	    $wnd.VK_HOME = @com.google.gwt.event.dom.client.KeyCodes::KEY_HOME;
	    $wnd.VK_LEFT = @com.google.gwt.event.dom.client.KeyCodes::KEY_LEFT;
	    $wnd.VK_PAGEDOWN = @com.google.gwt.event.dom.client.KeyCodes::KEY_PAGEDOWN;
	    $wnd.VK_PAGEUP = @com.google.gwt.event.dom.client.KeyCodes::KEY_PAGEUP;
	    $wnd.VK_RIGHT = @com.google.gwt.event.dom.client.KeyCodes::KEY_RIGHT;
	    $wnd.VK_SHIFT = @com.google.gwt.event.dom.client.KeyCodes::KEY_SHIFT;
	    $wnd.VK_TAB = @com.google.gwt.event.dom.client.KeyCodes::KEY_TAB;
        $wnd.VK_UP = @com.google.gwt.event.dom.client.KeyCodes::KEY_UP;
	}-*/;

	public static Cancellable run() throws Exception {
		return run(AppClient.getInstance());
	}

	public static Cancellable run(AppClient client) throws Exception {
		return run(client, extractPlatypusModules());
	}

	public static Cancellable run(AppClient client, Map<String, Element> start) throws Exception {
		if (LogConfiguration.loggingIsEnabled()) {
			platypusApplicationLogger = Logger.getLogger("platypusApplication");
			Formatter f = new PlatypusLogFormatter(true);
			Handler[] handlers = Logger.getLogger("").getHandlers();
			for (Handler h : handlers) {
				h.setFormatter(f);
			}
		}
		JSControls.initControls();
		JSContainers.initContainers();
		ModelJSControls.initModelControls();
		publish(client);
		AppClient.publishApi(client);
		loader = new Loader(client);
		Set<Element> indicators = extractPlatypusProgressIndicators();
		for (Element el : indicators)
			loaderHandlerRegistration.add(loader.addHandler(new ElementMaskLoadHandler(el.<XElement> cast()) {
				public void loaded(String anItemName) {
					xDiv.unmask();
				};
			}));
		return startAppElements(client, start);
	}

	private static Set<Element> extractPlatypusProgressIndicators() {
		Set<Element> platypusIndicators = new HashSet<Element>();
		XElement xBody = Utils.doc.getBody().cast();
		String platypusModuleClass = "platypusIndicator";
		if (platypusModuleClass.equals(xBody.getClassName()))
			platypusIndicators.add(xBody);

		NodeList<Element> divs = xBody.select("." + platypusModuleClass);// Utils.doc.getElementsByTagName("div");
		if (divs != null) {
			for (int i = 0; i < divs.getLength(); i++) {
				Element div = divs.getItem(i);
				platypusIndicators.add(div);
			}
		}
		return platypusIndicators;
	}

	private static Map<String, Element> extractPlatypusModules() {
		Map<String, Element> platypusModules = new HashMap<String, Element>();
		XElement xBody = Utils.doc.getBody().cast();
		String platypusModuleClass = "platypusModule";
		NodeList<Element> divs = xBody.select("." + platypusModuleClass);// Utils.doc.getElementsByTagName("div");
		if (divs != null) {
			for (int i = 0; i < divs.getLength(); i++) {
				Element div = divs.getItem(i);
				if (div.getId() != null && !div.getId().isEmpty()) {
					platypusModules.put(div.getId(), div);
				}
			}
		}
		String url = Document.get().getURL();
		if (url != null) {
			int pos = url.indexOf('#');
			if (pos > -1)
				platypusModules.put(url.substring(pos + 1), null);
		}
		return platypusModules;
	}

	protected static native void onReady()/*-{
		if ($wnd.platypus.ready)
			$wnd.platypus.ready();
	}-*/;

	protected static Cancellable startAppElements(AppClient client, final Map<String, Element> aMarkupStart) throws Exception {
		if (aMarkupStart == null || aMarkupStart.isEmpty()) {
			return client.getStartElement(new StringCallbackAdapter() {

				protected Cancellable loadings;

				@Override
				protected void doWork(String aResult) throws Exception {
					if (aResult != null && !aResult.isEmpty()) {
						Collection<String> results = new ArrayList<String>();
						results.add(aResult);
						loadings = loader.load(results, new ExecuteApplicationCallback(results));
					} else {
						onReady();
					}
				}

				@Override
				public void cancel() {
					super.cancel();
					if (loadings != null) {
						loadings.cancel();
					}
				}
			});
		} else {
			Set<String> modulesIds = aMarkupStart.keySet();
			for (final String elId : modulesIds) {
				Element el = aMarkupStart.get(elId);
				if (el != null) {
					loaderHandlerRegistration.add(loader.addHandler(new ElementMaskLoadHandler(el.<XElement> cast())));
				}
			}
			return loader.load(modulesIds, new ExecuteApplicationCallback(modulesIds));
		}
	}

	/**
	 * Avoiding parallel Loader.load() calls like this:
	 * require(["Module1", "Module2", "Module3", "Module4"], function(){});
	 * require(["Module0", "Module2", "Module5", "Module7"], function(){});
	 * Here, loader will be called twice form "Module2" in parallel.
	 */
	protected static boolean requiring;

	protected static class RequireProcess {
		public JavaScriptObject deps;
		public JavaScriptObject onSuccess;
		public JavaScriptObject onFailure;

		public RequireProcess(JavaScriptObject aDeps, final JavaScriptObject aOnSuccess, final JavaScriptObject aOnFailure) {
			deps = aDeps;
			onSuccess = aOnSuccess;
			onFailure = aOnFailure;
		}
	}

	protected static List<RequireProcess> requireProcesses = new ArrayList<RequireProcess>();

	public static void require(final JavaScriptObject aDeps, final JavaScriptObject aOnSuccess, final JavaScriptObject aOnFailure) {
		final Set<String> deps = new HashSet<String>();
		JsArrayString depsValues = aDeps.<JsArrayString> cast();
		for (int i = 0; i < depsValues.length(); i++) {
			String dep = depsValues.get(i);
			if (!loader.isLoaded(dep))
				deps.add(dep);
		}
		if (!requiring) {
			requiring = true;
			try {
				loader.prepareOptimistic();
				loader.load(deps, new CancellableCallbackAdapter() {

					@Override
					protected void doWork() throws Exception {
						requiring = false;
						try {
							if (deps.isEmpty() || loader.isLoaded(deps)) {
								if(aOnSuccess != null)
									Utils.invokeJsFunction(aOnSuccess);
								else
									Logger.getLogger(Application.class.getName()).log(Level.WARNING, "Require succeded, but callback is missing. Required modules are: "+aDeps.toString());
							} else {
								if(aOnFailure != null)
									Utils.invokeJsFunction(aOnFailure);
								else
									Logger.getLogger(Application.class.getName()).log(Level.WARNING, "Require failed and callback is missing. Required modules are: "+aDeps.toString());
							}
						} finally {
							if (!requireProcesses.isEmpty()) {
								RequireProcess p = requireProcesses.remove(0);
								assert p != null;
								require(p.deps, p.onSuccess, p.onFailure);
							}
						}
					}
				});
			} catch (Exception ex) {
				Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			requireProcesses.add(new RequireProcess(aDeps, aOnSuccess, aOnFailure));
		}
	}
}

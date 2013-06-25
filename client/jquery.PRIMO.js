/**
 * A PRIMO convenience library
 * dependencies: PRIMO RestFull API
 * dependencies: jquery.base64
 */

(function ($) {
    /**
     * FrontEnd Identifier. If you have multiple FrontEnds you can use Apache to set a Header
     * Apache configuration:
     *                 Header add X-LIMO-FE-ENVIRONMENT "TEST"
     */
    var limoFrontEndId = 'unknown';
    /**
     * Raw session data from restful service (private)
     */
    var sessionData = (function () {
        var _sessionData = {};

        (function () {
            jQuery.ajax({
                async:false,
                type:'get',
                dataType:'json',
                url:'/primo_library/libweb/rest/session',
                success:function (data) {
                    _sessionData = data;
                },
                complete:function (xhr, status) {
                    var http_headers = xhr.getAllResponseHeaders();
                    limoFrontEndId = xhr.getResponseHeader('X-LIMO-FE-ENVIRONMENT');
                },
                error:function (xhr, status, errorThrown) {
                    if (errorThrown === 'Not Found') {
                        alert('Unable to load session data. You need the PRIMO restfull API for this to work');
                    }
                }
            });
        }());

        return _sessionData;
    }());

    /**
     * Private method to load the PNX or orginal XML record from the search engine
     * @method _getRemoteRecord
     * @param [type] type of record to return must be one of xml,ris,pnx
     * @param [format] type of data that gets returned must be one of text, xml
     * @param [id] record id
     * @return [Object] remote record
     */
    function _getRemoteRecord(type, format, id) {
        var remoteRecord = '';
        var available_formats = ['xml', 'pnx', 'ris', 'deeplink'];

        if (jQuery.inArray(type, available_formats)) {
            // Encode record number if it contains a 'slash'
            if (id.match(/\W/g) != null) {
                id = jQuery.base64Encode(id) + "?encode=b64";
            }

            jQuery.ajax({
                async:false,
                type:'get',
                dataType:format,
                url:'/primo_library/libweb/rest/records/' + type + '/' + id,
                success:function (data) {
                    remoteRecord = data;
                },
                error:function (xhr, status, errorThrown) {
                    if (errorThrown === 'Not Found') {
                        alert('Unable to load record data. You need the PRIMO restfull API for this to work');
                    }
                }
            });
        }
        else {
            alert('Available formats for this record on the server: ' + available_formats);
        }
        ;
        return remoteRecord;
    };

    function _getIsDedupRecord(id){
	return id.search(/^dedupmrg/) != -1;
    }


    function _lookupRecordIDsWithDedupID(id) {
        var remoteRecord = '';

	jQuery.ajax({
	   async:false,
	   type:'get',
           dataType:'json',
	   url:'/primo_library/libweb/rest/records/dedupid2recordids/' + id,
           success:function (data) {
            remoteRecord = data;
           },
           error:function (xhr, status, errorThrown) {
	     if (errorThrown === 'Not Found') {
	       alert('Unable to load data. You need the PRIMO restfull API for this to work');
             }
           }
        });

	return remoteRecord;
    }

    /**
     * Private method to retrieve the material type of a record.
     * @method _materialType
     * @param [object] the record object
     * @return [string] the material type
     */
    function _materialType(record) {
        var materialType = '';
        if (record.id.substring(0, 2) === 'TN') {
            materialType = record.find('.EXLThumbnailCaption').text().trim().toLowerCase().replace(' ', '_');
        }
        else {
            pnx = _getRemoteRecord('pnx', 'text', record.id);
            materialType = jQuery(pnx).find('type').text()
        }
        return materialType;
    }

    ;

    //a = jQuery.getScript('http://127.0.0.1/~mehmetc/jquery.PRIMO.js')
    function _tab(record, i) {
        var tab = null;

        if (typeof(i) == 'number') {
            tab = record.find('.EXLResultTab')[i];
        }
        else if (typeof(i) == 'string') {
            tab = record.find('.EXLResultTab:contains("' + i + '")');
        }

        if (tab !== null) {
            var tabName = jQuery(tab).find('a').text().trim();
            var container = null;
            jQuery.each(tabName.toLowerCase().replace(/\s/g, '').split('&'), function () {
                c = record.find('*[class*="Container-' + this + '"]');

                if (c.length > 0) {
                    container = c;
                }
            });

            tab.index = i;
            tab.name = tabName;
            tab.container = container;
            tab.isOpen = function () {
                return jQuery(tab).hasClass('EXLResultSelectedTab');
            };
            tab.close = function () {
                if (!jQuery.PRIMO.isFullDisplay()) {
                    record.find('.EXLResultSelectedTab').removeClass('EXLResultSelectedTab');
                    record.find('.EXLTabsRibbon').addClass('EXLTabsRibbonClosed');
                    tab.container.hide();
                }
            };
            tab.open = function (content, options) {
                defaults = {
                    reload:false,
                    headerContent:'',
                    url:'#'
                };
                var o = jQuery.extend(defaults, options);
                currentTab = record.getTabByName(tabName);
                record.find('.EXLTabsRibbonClosed').removeClass('EXLTabsRibbonClosed');
                record.find('.EXLResultSelectedTab').removeClass('EXLResultSelectedTab');
                jQuery(currentTab).addClass('EXLResultSelectedTab');
                record.find('.EXLResultTabContainer').hide();
                currentTab.container.show();

                if ((!currentTab.container.data('loaded')) || (o.reload)) {
                    var popOut = '<div class="EXLTabHeaderContent">' + o.headerContent + '</div><div class="EXLTabHeaderButtons"><ul><li class="EXLTabHeaderButtonPopout"><span></span><a href="' + o.url + '" target="_blank"><img src="../images/icon_popout_tab.png" /></a></li><li></li><li class="EXLTabHeaderButtonCloseTabs"><a href="#" title="hide tabs"><img src="../images/icon_close_tabs.png" alt="hide tabs"></a></li></ul></div>';
                    var header = '<div class="EXLTabHeader">' + popOut + '</div>';
                    var body = '<div class="EXLTabContent">' + content + '</div>'
                    currentTab.container.html(header + body);
                    currentTab.container.data('loaded', true);
                }
            }
        }

        return tab;
    }

    ;

    function _tabs(record) {
        var tabData = [];
        var tab_count = record.find('.EXLResultTab').length;

        for (k = 0; k < tab_count; k++) {
            tabData.push(_tab(record, k));
        }

        if (tabData.add == null) {
            tabData.add = function (tabName, options) {
                options.record = record;
                _addTab(tabName, options);
            }
        }

        tabData.getNames = function () {
            tabNames = [];
            jQuery.each(tabData, function () {
                tabNames.push(this.name);
            });
            return tabNames;
        };

        tabData.getEnabled = function () {
            return jQuery.map(record.find('.EXLResultTab'),
                function (tab) {
                    tab = jQuery(tab);
                    if (tab.css('display') != 'none') {
                        return jQuery(tab).text().trim();
                    }
                    return null;
                });
        };

        return tabData;
    }

    function _addTab(tabName, options) {
        defaults = {
            record:null,
            state:'disabled',
            css:tabName.replace(' ', '').toLowerCase() + 'Tab',
            url:'#',
            tooltip:'',
            headerContent:'',
            click:function (e) {
                alert('To be implemented...');
            }
        }

        var o = jQuery.extend(defaults, options);

        if (jQuery.inArray(tabName, o.record.tabs.getNames()) < 0) { // not in tablist -> new tab
            var customTab = '<li class="EXLResultTab ' + o.css + '">';
            customTab += '  <span style="display:' + (o.state == 'disabled' ? 'block' : 'none') + '">' + tabName + '</span>';
            customTab += '  <a style="display:' + (o.state == 'disabled' ? 'none' : 'block') + '" title="' + o.tooltip + '" href="' + o.url + '">' + tabName + '</a>';
            customTab += '</li>';
            var customTabContainer = '<div class="EXLResultTabContainer EXLContainer-' + o.css + '"></div>';

            o.record.find('.EXLResultTab').last().after(customTab);
            if (o.record.hasClass('EXLSummary')) {
                o.record.append(customTabContainer);
            } else {
                o.record.find('.EXLSummary').append(customTabContainer);
            }

            var customClassQuery = '.' + o.css + ' a';
            o.record.find(customClassQuery).click(function (e) {
                e.preventDefault();
                if (o.state == 'enabled') {
                    tab = o.record.getTabByName(tabName);
                    o.click(e, tab, o.record, o);
                }
            });
        }
        else {

        }

        o.record.tabs = _tabs(o.record);
    }

    /**
     * Private method to build a pointer to the record and enhance it
     * @method _record
     * @param [Number] i record on page
     * @return [Object] enhanced record pointer
     */
    function _record(i) {
        var record = jQuery(jQuery('.EXLResult')[i]);
        record.index = i;
        record.id = record.find('.EXLResultRecordId[name]').attr('name');
        record.title = record.find('.EXLResultTitle').text().trim();
        record.openUrl = record.find('.EXLMoreTab a').attr('href');
        record.isRemoteRecord = (record.id.substring(0, 2) === 'TN');
        record.getPNX = function () {
            return _getRemoteRecord('pnx', 'text', record.id);
        };
        record.getXML = function () {
            return _getRemoteRecord('xml', 'text', record.id);
        };
        record.getRIS = function () {
            return _getRemoteRecord('ris', 'text', record.id);
        };
        record.getDeepLink = function () {
            return _getRemoteRecord('deeplink', 'text', record.id);
        };
        record.materialType = _materialType(record);
        record.tabs = _tabs(record);

        record.getTabByName = function (name) {
            return _tab(record, name);
        };

	record.isDedupRecord = _getIsDedupRecord(record.id);

        record.lookupRecordIDsWithDedupID = function () {
		if (_getIsDedupRecord(record.id)) {
			return _lookupRecordIDsWithDedupID(record.id);		   
		}
		return [];
	};


        return record;
    }

    /**
     * @module jQuery.PRIMO
     */
    jQuery.PRIMO = {
        /**
         * @property session
         * @type Object
         */
        session:{
            /**
             * Contains FrontEnd Identifier
             *
             * @property frontEndId
             * @type String
             */
            frontEndId:(function () {
                return limoFrontEndId
            }()),
            /**
             * Current View Name
             *
             * @property currentViewName
             * @type String
             */
            currentViewName:(function () {
                return sessionData.view;
            }()),
            /**
             * Current View Language
             *
             * @property currentViewLanguage
             * @type String
             */
            currentViewLanguage:(function () {
                return sessionData.interfaceLanguage;
            }()),
	    /**
	     * Current View PDS url
             *
	     * @property currentViewPDSUrl
	     * @type String
            */
	    currentViewPDSUrl:(function() {
		return sessionData.pdsUrl;
	    }()), 
            /**
             * Institution data
             * @property institution
             * @type Object
             */
            institution:{
                /**
                 * @property name
                 * @type String
                 */
                name:(function () {
                    return sessionData.institutionName;
                }()),
                /**
                 * @property nameByIP
                 * @type String
                 */
                nameByIP:(function () {
                    return sessionData.institutionNameByIP;
                }()),
                /**
                 * @property nameByVIEW
                 * @type String
                 */
                nameByVIEW:(function () {
                    return sessionData.institutionNameByView;
                }()),
                /**
                 * @property code
                 * @type String
                 */
                code:(function () {
                    return sessionData.institutionCode;
                }()),
                /**
                 * @property codeByIP
                 * @type String
                 */
                codeByIP:(function () {
                    return sessionData.institutionCodeByIP;
                }()),
                /**
                 * @property codeByVIEW
                 * @type String
                 */
                codeByVIEW:(function () {
                    return sessionData.institutionCodeByView;
                }())
            },
            /**
             * SFX properties
             * @property sfx
             * @type Object
             */
            sfx:{
                /**
                 * @property code
                 * @type String
                 */
                code:(function () {
                    return sessionData.sfxInstitutionCode
                }())
            },
            /**
             * METALIB properties
             * @property metalib
             * @type Object
             */
            metalib:{
                /**
                 * @property code
                 * @type String
                 */
                code:(function () {
                    return sessionData.metalibInstitutionCode
                }())
            },
            /**
             * USER data
             * @property user
             * @type Object
             */
            user:{
                /**
                 * Session id of the user
                 * @property id
                 * @type String
                 */
                id:(function () {
                    return sessionData.userInfo.userId
                }()),
                /**
                 * User name defaults to anonymous when not logged on.
                 * @property name
                 * @type String
                 */
                name:(function () {
                    return sessionData.userInfo.userName
                }()),
                /**
                 * @property group
                 * @type Object
                 */
                group:{
                    /**
                     * Borrower group id
                     * @property id
                     * @type String
                     */
                    id:(function () {
                        return sessionData.userInfo.borGroupId
                    }()),
                    /**
                     * Borrower group name
                     * @property name
                     * @type String
                     */
                    name:(function () {
                        return sessionData.userInfo.borGroup
                    }())
                }
            }
        },
        records:(function () {
            var records_count = jQuery('.EXLResult').length;
            var data = [];

            for (j = 0; j < records_count; j++) {
                data.push(_record(j));
            }
            return data;
        }()),
        /**
         * Test if the FrontEnd is running on TEST environment
         *
         * @method isTestFrontEnd
         * @return Boolean
         */
        isTestFrontEnd:function () {
            return sessionData.frontEndId === 'TEST';
        },
        /**
         * Is the user on campus
         *
         * @method isOnCampus
         * @return Boolean
         */
        isOnCampus:function () {
            return sessionData.onCampus;
        },
        /**
         * Is the user loggon on
         *
         * @method isLoggedOn
         * @return Boolean
         */
        isLoggedOn:function () {
            return sessionData.loggedOn;
        },
        /**
         * Are we looking at the record in FULL Display
         * @method isFullDisplay
         * @return Boolean
         */
        isFullDisplay:function () {
            return jQuery('.EXLFullView').size() > 0;
        },
        /**
         * Get a record by supplying an object like a location tab
         * @method getRecordByObject
         * @param [Object] the object you want to get the record for can be a jQuery or DOM object
         * @return [Object] null when not found or record
         */
        getRecordByObject:function (referenceObject) {
            var primoObject = null;
            if (referenceObject instanceof jQuery) {
                primoObject = referenceObject;
            }
            else {
                primoObject = jQuery(referenceObject);
            }

            if (primoObject != undefined && primoObject != null && primoObject.parents('.EXLResult').length > 0 && primoObject.parents('.EXLResult').attr('id') != undefined) {
                return _record(primoObject.parents('.EXLResult').attr('id').replace(/[^0-9]/g, ''));
            }
            else {
                return _record(0);
            }
            return null;
        }
    }
})(jQuery);

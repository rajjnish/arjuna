'''
This file is a part of Arjuna
Copyright 2015-2020 Rahul Verma

Website: www.RahulVerma.net

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
'''

import re
import os

from enum import Enum, auto
from abc import abstractmethod

from arjuna.core.enums import GuiAutomationContext
from arjuna.core.exceptions import GuiLabelNotPresentError
from arjuna.interact.gui.auto.finder.emd import GuiElementMetaData, Locator, ImplWith
from arjuna.core.yaml import YamlFile

class FileFormat(Enum):
    # GNS = auto()
    # MGNS = auto()
    XLS = auto()
    XLSX = auto()
    YAML = auto()

class GuiNamespaceLoaderFactory:

    # Returns GuiNamespaceLoader
    @classmethod
    def create_namespace_loader(cls, config, ns_file_path):
        from arjuna.core.enums import ArjunaOption
        multi_context_enabled = config.value(ArjunaOption.GUIAUTO_DEF_MULTICONTEXT)
        context = multi_context_enabled and None or config.guiauto_context
        _, file_extension = os.path.splitext(ns_file_path)
        ext = file_extension.upper()[1:]
        considered_path = ns_file_path
        try:
            file_format = FileFormat[ext]
        except:
            raise Exception("Unsupported format for namespace: {}".format(file_extension))
        else:
            full_file_path = ns_file_path
            if os.path.isdir(full_file_path):
                raise Exception("Namespace file path is a directory and not a file: {}".format(considered_path))
            elif not os.path.isfile(full_file_path):
                raise Exception("Namespace file path is invalid: {}".format(considered_path))
            # if file_format == FileFormat.GNS:
            #     if multi_context_enabled:
            #         return MGNSFileLoader(full_file_path)
            #     else:
            #         return GNSFileLoader(full_file_path, context)
            if file_format == FileFormat.YAML:
                return YamlGnsLoader(full_file_path, context)
            else:
                raise Exception("Unsupported format for namespace: {}".format(file_extension))


class GuiNamespace:

    def __init__(self, name):
        self.__name = name
        # dict <string, dict<GuiAutomationContext, GuiElementMetaData>>
        self.__ns = {}

    def add_element_meta_data(self, name, context, raw_locators):
        emd = GuiElementMetaData.create_lmd(*raw_locators)
        #emd = GuiElementMetaData(raw_locators, process_args=False)
        name = name.lower()
        if not self.has(name):
            self.__ns[name] = {}
        self.__ns[name][context] = emd

    def has(self, name):
        return name.lower() in self.__ns

    def has_context(self, name, context):
        if self.has(name):
            return context in self.__ns[name.lower()]
        return False

    # Needs to be thread-safe
    # Returns emd for a context for a given gui name
    def get_meta_data(self, label, context):
        if not self.has(label):
            raise GuiLabelNotPresentError(self.__name, label)
        elif not self.has_context(label, context):
            raise GuiLabelNotPresentError(self.__name, label, context)
        
        return self.__ns[label.lower()][context]


class BaseGuiNamespaceLoader:

    def __init__(self, name):
        self.__name = name
        self.__namespace = GuiNamespace(name)

    @property
    def name(self):
        return self.__name

    @property
    def namespace(self):
        return self.__namespace

    # Needs to be thread safe
    def add_element_meta_data(self, name, context, locators):
        self.__namespace.add_element_meta_data(name, context, locators)

    def _raise_notafile_exception(self, file_path):
        raise Exception("{} is not a file.".format(file_path))

    def _raise_filenotfound_exception(self, file_path):
        raise Exception("{} is not a valid file path.".format(file_path))

    def _raise_relativepath_exception(self, file_path):
        raise Exception("Gui namespace loader does not accept relative file path. {} is not a full file path.".format(file_path))


class YamlGnsLoader(BaseGuiNamespaceLoader):

    def __init__(self, ns_file_path, context):
        super().__init__(os.path.basename(ns_file_path))
        self.__context = context
        self.__ns_file = None
        self.__ns_path = None

        #Map<String, Map<GuiAutomationContext,List<Locator>>>
        self.__ns = {}

        if not os.path.isabs(ns_file_path):
            super()._raise_relativepath_exception(ns_file_path)
        elif not os.path.exists(ns_file_path):
            super()._raise_filenotfound_exception(ns_file_path)
        elif not os.path.isfile(ns_file_path):
            super()._raise_notafile_exception(ns_file_path)

        self.__ns_path = ns_file_path
        self.__contexts = [context]

        self.__withx = None

        # self.__process()

    def load(self):
        
        from arjuna import Arjuna
        from arjuna.configure.impl.validator import Validator
        yaml = YamlFile(self.__ns_path)

        if yaml.is_empty(): return

        if not yaml.has_section("labels"):
            # print("No labels configured. Skipping...")
            return

        from arjuna.interact.gui.auto.finder.withx import WithX
        if yaml.has_section("withx"):
            self.__withx = WithX(yaml.get_section("withx").as_map())
        else: 
            self.__withx = WithX()

        common_withx = Arjuna.get_withx_ref()

        for label, label_map in yaml.get_section("labels").as_map().items():
            Validator.arjuna_name(label)
            self.__ns[label.lower()] = dict()
            self.__ns[label.lower()][self.__context] = []
            for loc, loc_obj in label_map.items():
                loc = loc.lower()
                wtype, wvalue = None, None
                if not self.__withx.has_locator(loc) and not common_withx.has_locator(loc):
                    wtype, wvalue = loc.upper(), loc_obj
                else:
                    if self.__withx.has_locator(loc):
                        wx = self.__withx
                    elif common_withx.has_locator(loc):
                        wx = common_withx
                    else:
                        raise Exception("No WithX locator with name {} found. Check GNS file at {}.".format(name, self.__ns_path))
                    wtype, wvalue = wx.format(loc, loc_obj)

                iloc = ImplWith(wtype=wtype, wvalue=wvalue, named_args=dict(), has_content_locator=False)
                self.__ns[label.lower()][self.__context].append(iloc)

            if not self.__ns[label.lower()][self.__context]:
                raise Exception("No locators defined for label: {}".format(label))

        if yaml.has_section("load"):
            self.__load_targets = yaml.get_section("load")

            if self.__load_targets.has_section("root"):
                self.__ns["__root__"] = dict()
                self.__ns["__root__"][self.__context] = list()
                self.__ns["__root__"][self.__context].extend(self.__ns[self.__load_targets.get_section("root").as_map()][self.__context])

            if self.__load_targets.has_section("anchor"):
                self.__ns["__anchor__"] = dict()
                self.__ns["__anchor__"][self.__context] = list()
                self.__ns["__anchor__"][self.__context].extend(self.__ns[self.__load_targets.get_section("anchor").as_map()][self.__context])

        for ename, context_data in self.__ns.items():
            for context, locators in context_data.items():
                self.add_element_meta_data(ename, context, locators)
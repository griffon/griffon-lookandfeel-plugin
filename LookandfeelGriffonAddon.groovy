/*
 * Copyright 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import griffon.lookandfeel.LookAndFeelInfo
import griffon.lookandfeel.LookAndFeelManager
import griffon.lookandfeel.LookAndFeelProvider

import java.awt.Toolkit
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import javax.swing.UIManager
import javax.swing.KeyStroke

import griffon.core.GriffonApplication
import griffon.util.RunMode

import static griffon.util.GriffonApplicationUtils.isMacOSX

/**
 * @author Andres Almiray
 */
class LookandfeelGriffonAddon {
    void addonInit(GriffonApplication app) {
        if(RunMode.current == RunMode.APPLET || RunMode.current == RunMode.WEBSTART) {
            UIManager.put('ClassLoader', app.class.classLoader)
        }

        LookAndFeelManager.instance.loadLookAndFeelProviders()

        String provider = app.config.lookandfeel.lookAndFeel ?: 'System'
        String theme    = app.config.lookandfeel.theme ?: (isMacOSX? 'System': 'Nimbus')

        LookAndFeelManager.instance.with {
            LookAndFeelProvider lafProvider = getLookAndFeelProvider(provider)
            LookAndFeelInfo lafInfo = getLookAndFeelInfo(lafProvider, theme)
            if(!lafInfo) {
                lafProvider = getLookAndFeelProvider('System')
                lafInfo     = getLookAndFeelInfo(lafProvider, (isMacOSX? 'System': ' Nimbus'))
            }
            apply(lafInfo, app)
        }
        app.config.lookandfeel.props.each { key, value ->
            javax.swing.UIManager.put(key, value)
        }

        String keyStrokeText = app.config?.lookandfeel?.keystroke
        if(keyStrokeText instanceof ConfigObject || !keyStrokeText || keyStrokeText.toLowerCase() == 'none') return
        KeyStroke trigger = KeyStroke.getKeyStroke(keyStrokeText)
        if(!trigger) return

        KeyboardFocusManager.currentKeyboardFocusManager.addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                KeyStroke k = KeyStroke.getKeyStrokeForEvent(e)
                if(e.getID() == KeyEvent.KEY_RELEASED &&
                   k.keyCode == trigger.keyCode &&
                   k.modifiers == trigger.modifiers) {
                    LookAndFeelManager.instance.showLafDialog(app)
                    return true
                }
                return false
            }
        })
    }

    Map mvcGroups = [
        'LookAndFeelSelector': [
            model     : 'griffon.plugins.lookandfeel.LookAndFeelSelectorModel',
            view      : 'griffon.plugins.lookandfeel.LookAndFeelSelectorView',
            controller: 'griffon.plugins.lookandfeel.LookAndFeelSelectorController'
        ]
    ]
}

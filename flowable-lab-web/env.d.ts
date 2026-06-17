/// <reference types="vite/client" />

declare module 'bpmn-js' {
  interface BpmnViewerOptions {
    container: HTMLElement
    propertiesPanel?: { parent: HTMLElement }
    additionalModules?: any[]
    moddleExtensions?: Record<string, any>
  }
  class BpmnViewer {
    constructor(options: BpmnViewerOptions)
    importXML(xml: string): Promise<any>
    get(module: string): any
  }
  export default BpmnViewer
}

declare module 'bpmn-js/lib/Modeler' {
  import BpmnViewer from 'bpmn-js'
  class Modeler extends BpmnViewer {
    saveXML(options?: { format?: boolean }): Promise<{ xml?: string }>
    destroy(): void
  }
  export default Modeler
}

declare module 'bpmn-js-properties-panel' {
  export const BpmnPropertiesPanelModule: any
  export const BpmnPropertiesProviderModule: any
  export const CamundaPlatformPropertiesProviderModule: any
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

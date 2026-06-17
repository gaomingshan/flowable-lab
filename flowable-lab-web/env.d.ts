/// <reference types="vite/client" />

/// <reference types="vite/client" />

declare module 'bpmn-js' {
  class BpmnViewer {
    constructor(options: { container: HTMLElement })
    importXML(xml: string): Promise<void>
    get(module: string): any
  }
  export default BpmnViewer
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

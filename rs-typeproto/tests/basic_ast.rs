use cause_typeproto::ast::*;

#[test]
fn hello_world() {
    let file_node = AstNode::<FileNode> {
        position: DocumentRange::default(),
        breadcrumbs: Breadcrumbs(vec![]),
        node: FileNode {
            declarations: vec![AstNode {
                position: DocumentRange::default(),
                breadcrumbs: Breadcrumbs(vec![
                    BreadcrumbEntry::Name("declarations"),
                    BreadcrumbEntry::Index(0),
                ]),
                node: DeclarationNode::Function(FunctionDeclarationNode {
                    name: AstNode {
                        position: DocumentRange::default(),
                        breadcrumbs: Breadcrumbs(vec![
                            BreadcrumbEntry::Name("declarations"),
                            BreadcrumbEntry::Index(0),
                            BreadcrumbEntry::Name("name"),
                        ]),
                        node: Identifier("main".to_string()),
                    },
                    return_type: None,
                    body: AstNode {
                        position: DocumentRange::default(),
                        breadcrumbs: Breadcrumbs(vec![
                            BreadcrumbEntry::Name("declarations"),
                            BreadcrumbEntry::Index(0),
                            BreadcrumbEntry::Name("body"),
                        ]),
                        node: BodyNode::BlockBody(BlockBodyNode { statements: vec![] }),
                    },
                }),
            }],
        },
    };
    println!("{:#?}", file_node);
}

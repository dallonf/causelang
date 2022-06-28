pub mod analyzer;
pub mod ast;
pub mod breadcrumb_walk;
mod core_globals;
pub mod parse;
pub mod resolver;
pub mod types;

#[cfg(test)]
mod tests {
    #[test]
    fn it_works() {
        let result = 2 + 2;
        assert_eq!(result, 4);
    }
}

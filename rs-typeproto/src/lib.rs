pub mod analyzer;
pub mod ast;
pub mod parse;
pub mod types;
pub mod resolver;

#[cfg(test)]
mod tests {
    #[test]
    fn it_works() {
        let result = 2 + 2;
        assert_eq!(result, 4);
    }
}

use std::sync::Arc;

pub fn arc_into<Input: ToOwned<Owned = Input>, Output: From<Input>>(
    input: &Arc<Input>,
) -> Arc<Output> {
    Arc::new(input.as_ref().to_owned().into())
}
